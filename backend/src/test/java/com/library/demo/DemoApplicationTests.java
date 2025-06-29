package com.library.demo;

import com.jayway.jsonpath.JsonPath;
import com.library.demo.models.Role;
import com.library.demo.models.User;
import com.library.demo.repository.RoleRepository;
import com.library.demo.repository.UserRepository;
import com.library.demo.services.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;

	private final String loginJson = """
        {
            "email": "user@example.com",
            "password": "password123"
        }
        """;

	@BeforeAll
	static void setup(@Autowired RoleRepository roleRepository,
					  @Autowired UserRepository userRepository,
					  @Autowired PasswordEncoder passwordEncoder) {
		Role role = Role.builder().name("USER").build();
		roleRepository.save(role);
		User user = User.builder()
				.email("user@example.com")
				.password(passwordEncoder.encode("password123"))
				.role(role)
				.build();
		userRepository.save(user);

	}

	@Test
	void testRegisterUser() throws Exception {

		String userJson = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson))
				.andExpect(status().isOk());
	}

	@Test
	void testRegisterUserWithExistingEmail() throws Exception {
		String existingUserJson = """
        {
            "email": "user@example.com",
            "password": "password123"
        }
        """;

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(existingUserJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void login_shouldReturnValidJwtToken() throws Exception {

		MvcResult result = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();

		String token = JsonPath.read(responseBody, "$.token");

		Claims claims = jwtService.extractAllClaims(token);

		assertEquals("user@example.com", claims.getSubject());
		assertEquals("USER", claims.get("Role"));
	}


	@Test
	void login_shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
		String wrongJson = """
        {
            "email": "user@example.com",
            "password": "wrongpassword"
        }
        """;

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(wrongJson))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.token").doesNotExist());
	}



	@Test
	void addBookToReadList_shouldReturnCreated() throws Exception {

		MvcResult loginResult = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();

		String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

		String bookJson = """
        {
            "googleBookId": "test-google-book-id-001",
            "title": "Test Book Title",
            "authors": "Author One, Author Two",
            "description": "Test description",
            "thumbnail": "http://example.com/thumb.jpg"
        }
        """;

		mockMvc.perform(post("/api/books")
						.contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + token)
						.content(bookJson))
				.andExpect(status().isCreated());

	}

	@Test
	void getReadBooks_shouldReturnEmptyListWhenNoBooks() throws Exception {

		MvcResult loginResult = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();

		String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

		mockMvc.perform(get("/api/books")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}



}
