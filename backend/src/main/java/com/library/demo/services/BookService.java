package com.library.demo.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.demo.dto.BookSearchResponse;
import com.library.demo.dto.PagedUserBooksResponse;
import com.library.demo.dto.SimplifiedBook;
import com.library.demo.dto.UserBookDto;
import com.library.demo.exceptions.GoogleBooksApiException;
import com.library.demo.exceptions.ResourceNotFoundException;
import com.library.demo.exceptions.UnauthorizedException;
import com.library.demo.models.Book;
import com.library.demo.models.User;
import com.library.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final String GOOGLE_BOOKS_API ;
    private final String API_KEY ;

    private final RestTemplate restTemplate;

    private final BookRepository bookRepository;


    public BookService(@Value("${google.books.api.url}") String apiUrl,
                       @Value("${google.books.api.key}") String apiKey,
                       BookRepository bookRepository,
                       RestTemplate restTemplate) {
        this.bookRepository = bookRepository;
        this.API_KEY = apiKey;
        this.GOOGLE_BOOKS_API = apiUrl;
        this.restTemplate = restTemplate;
    }


    public BookSearchResponse searchBooks(String q, int page, int size) {
        int startIndex = page * size;
        String url = GOOGLE_BOOKS_API + "?q=" + q + "&startIndex=" + startIndex + "&maxResults=" + size + "&key=" + API_KEY;
        String json = restTemplate.getForObject(url, String.class);

        List<SimplifiedBook> books = new ArrayList<>();
        int totalItems = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            totalItems = root.path("totalItems").asInt(0);
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(mapJsonToSimplifiedBook(item.toString()));
                }
            }

        } catch (Exception e) {
            throw new GoogleBooksApiException("Błąd pobierania danych z Google Books API", e);
        }

        return BookSearchResponse.builder()
                .books(books)
                .totalItems(totalItems)
                .build();
    }

    public SimplifiedBook getBookById(String volumeId) {
        String url = GOOGLE_BOOKS_API + "/" + volumeId + "?key=" + API_KEY;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return mapJsonToSimplifiedBook(response.getBody());
    }

    public SimplifiedBook mapJsonToSimplifiedBook(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            String googleBookId = root.path("id").asText(); // teraz wyciągamy ID tutaj

            JsonNode volumeInfo = root.path("volumeInfo");

            String title = volumeInfo.path("title").asText("No title");
            String description = volumeInfo.path("description").asText("");
            String thumbnail = volumeInfo.path("imageLinks").path("thumbnail").asText("");

            String authors = "Unknown";
            JsonNode authorsNode = volumeInfo.path("authors");
            if (authorsNode.isArray()) {
                List<String> authorList = new ArrayList<>();
                for (JsonNode author : authorsNode) {
                    authorList.add(author.asText());
                }
                if (!authorList.isEmpty()) {
                    authors = String.join(", ", authorList);
                }
            }

            return new SimplifiedBook(
                    googleBookId,
                    title,
                    authors,
                    description,
                    thumbnail
            );
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas mapowania JSON na SimplifiedBook", e);
        }
    }

    public boolean hasUserReadBook(Long userId, String googleBookId) {
        return bookRepository.existsByGoogleBookIdAndUserId(googleBookId, userId);
    }


    public Book addReadBook(SimplifiedBook dto, User user) {

        if (hasUserReadBook(user.getId(), dto.googleBookId())) {
            throw new RuntimeException("Książka już jest w przeczytanych");
        }

        Book book = Book.builder()
                        .title(dto.title())
                        .googleBookId(dto.googleBookId())
                        .authors(dto.authors()).description(dto.description()).thumbnail(dto.thumbnail()).user(user).build();
        return bookRepository.save(book);
    }

    public List<UserBookDto> getBooksForUser(User user) {
        List<Book> books = bookRepository.findByUser(user);
        List<UserBookDto> result = new ArrayList<>();

        for (Book book : books) {
            result.add(new UserBookDto(
                    book.getId(),
                    book.getGoogleBookId(),
                    book.getTitle(),
                    book.getAuthors(),
                    book.getDescription(),
                    book.getThumbnail()
            ));
        }

        return result;
    }

    public PagedUserBooksResponse getPagedBooksForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.findByUser(user, pageable);

        List<UserBookDto> content = booksPage.getContent().stream()
                .map(book -> new UserBookDto(
                        book.getId(),
                        book.getGoogleBookId(),
                        book.getTitle(),
                        book.getAuthors(),
                        book.getDescription(),
                        book.getThumbnail()
                ))
                .toList();

        return new PagedUserBooksResponse(content, booksPage.getTotalElements());
    }

    public void deleteBookByIdForUser(Long id, User user) {
        Optional<Book> optionalBook = bookRepository.findById(id);

        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Książka o " + id + " nie istnieje aby ją usunąć");
        }

        Book book = optionalBook.get();

        if (!book.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Nie masz dostępu do tej książki");
        }

        bookRepository.delete(book);
    }



}
