package com.libraryhub.book.repository;

import com.libraryhub.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // We'll add custom queries here later if needed
    Optional<Book> findByFileName(String fileName);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findAllByOrderByTitleAsc(); // For A-Z
    List<Book> findAllByOrderByCreatedAtDesc(); // For latest to oldest
}
