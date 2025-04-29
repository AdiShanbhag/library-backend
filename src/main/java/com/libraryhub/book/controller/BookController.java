package com.libraryhub.book.controller;

import com.libraryhub.book.model.Book;
import com.libraryhub.book.response.ApiResponse;
import com.libraryhub.book.service.BookService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // Cloudinary Configuration is not needed for this controller (handled in the service)

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("description") String description) throws IOException {

        // Save the book with Cloudinary URL instead of local path
        Book uploadedBook = bookService.saveBook(file, title, author, description);

        ApiResponse<Book> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Book uploaded successfully!",
                uploadedBook
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return book != null ? new ResponseEntity<>(book, HttpStatus.OK)
                : new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        try {
            // Call the service method to delete the book
            bookService.deleteBook(bookId);

            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Book deleted successfully",
                    "Book with ID " + bookId + " has been deleted"
            );

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete book",
                    e.getMessage()
            );

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Void> downloadBook(@PathVariable Long id) {
        Optional<Book> optionalBook = Optional.ofNullable(bookService.getBookById(id));
        if (optionalBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = optionalBook.get();

        // Increment download count
        //book.setDownloadCount(book.getDownloadCount() + 1);

        // Redirect to the actual Cloudinary URL
        URI cloudinaryUrl = URI.create(book.getDownloadUrl());
        System.out.println("Before Download URL " + book.getDownloadUrl());
        System.out.println("Download URI " + cloudinaryUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(cloudinaryUrl).build();
    }


}