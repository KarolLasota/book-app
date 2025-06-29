package com.library.demo.services;

import com.library.demo.dto.AuthResponse;
import com.library.demo.dto.LoginRequest;
import com.library.demo.dto.RegisterRequest;
import com.library.demo.models.RefreshToken;
import com.library.demo.models.Role;
import com.library.demo.models.User;
import com.library.demo.repository.RefreshTokenRepository;
import com.library.demo.repository.RoleRepository;
import com.library.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<AuthResponse> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Użytkownik z takim adresem e-mail już istnieje.");
        }
        if (request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hasło musi mieć minimum 8 znaków");
        }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("brak roli USER"));
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(defaultRole)
                .build();
        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        ResponseCookie cookie = createHttpOnlyRefreshCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(jwt));

    }

    public ResponseEntity<AuthResponse> refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Błędny refresh token"));

        refreshTokenService.verifyExpiration(token);

        User user = token.getUser();
        String newAccessToken = jwtService.generateToken(user);
        ResponseCookie cookie = createHttpOnlyRefreshCookie(token.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(newAccessToken));
    }


    public ResponseEntity<AuthResponse>authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Błędne dane");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        ResponseCookie cookie = createHttpOnlyRefreshCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(jwt));
    }


    private ResponseCookie createHttpOnlyRefreshCookie(String tokenValue) {
        return ResponseCookie.from("refreshToken", tokenValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();
    }


}
