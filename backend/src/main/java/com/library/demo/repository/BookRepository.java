package com.library.demo.repository;

import com.library.demo.models.Book;
import com.library.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByUser(User user);

    Page<Book> findByUser(User user, Pageable pageable);

    boolean existsByGoogleBookIdAndUserId(String googleBookId, Long userId);
}
