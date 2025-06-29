package com.library.demo.controllers;


import com.library.demo.dto.BookSearchResponse;
import com.library.demo.dto.PagedUserBooksResponse;
import com.library.demo.dto.SimplifiedBook;
import com.library.demo.dto.UserBookDto;
import com.library.demo.models.User;
import com.library.demo.services.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/books")
public class BookController {


    private final BookService bookService;



    @GetMapping("/search")
    public ResponseEntity<BookSearchResponse> searchBooks(@RequestParam String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        BookSearchResponse books = bookService.searchBooks(q, page, size);
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<Void> addBookToReadList(@Valid @RequestBody SimplifiedBook bookDto, @AuthenticationPrincipal User user) {
        bookService.addReadBook(bookDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/books/read/{googleBookId}")
    public ResponseEntity<Boolean> hasUserReadBook(
            @AuthenticationPrincipal User user,
            @PathVariable String googleBookId) {
        boolean read = bookService.hasUserReadBook(user.getId(), googleBookId);
        return ResponseEntity.ok(read);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimplifiedBook> getBookById(@PathVariable("id") String volumeId) {
        SimplifiedBook book = bookService.getBookById(volumeId);
        return ResponseEntity.ok(book);
    }
    @GetMapping
    public ResponseEntity<List<UserBookDto>> getReadBooks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookService.getBooksForUser(user));
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedUserBooksResponse> getReadBooksPaged(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookService.getPagedBooksForUser(user, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id, @AuthenticationPrincipal User user) {
        bookService.deleteBookByIdForUser(id, user);
        return ResponseEntity.noContent().build();
    }


}
