package com.libraryhub.book.service;

import com.libraryhub.book.model.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BookService {

    Book saveBook(MultipartFile file, String title, String author, String description) throws IOException;

    void saveBook(Book book);

    Book getBookById(Long id);

    List<Book> getAllBooks();

    void deleteBook(Long id) throws IOException;

    public String uploadToCloudinary(MultipartFile file) throws IOException;

    ResponseEntity<byte[]> downloadBookById(Long id) throws IOException;

    List<Book> searchBooksByAuthor(String author);

    List<Book> searchBooksByTitle(String title);

    // New methods for sorting
    List<Book> getBooksSortedByTitle();         // A-Z for browse page

    List<Book> getBooksSortedByLatest();        // Latest to oldest for homepage

}
