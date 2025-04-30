package com.libraryhub.book.controller;

import com.libraryhub.book.model.Book;
import com.libraryhub.book.response.ApiResponse;
import com.libraryhub.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<byte[]> downloadBook(@PathVariable Long id) {
        try {
            return bookService.downloadBookById(id);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file".getBytes());
        }
    }

    @PutMapping("/books/increment-download/{id}")
    public ResponseEntity<?> incrementDownloadCount(@PathVariable Long id) {
        Optional<Book> optionalBook = Optional.ofNullable(bookService.getBookById(id));
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setDownloadCount(book.getDownloadCount() + 1);
            bookService.saveBook(book);
            return ResponseEntity.ok("Download count incremented");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam("author") String author) {
        List<Book> books = bookService.searchBooksByAuthor(author);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam("title") String title) {
        List<Book> books = bookService.searchBooksByTitle(title);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/sorted/title")
    public ResponseEntity<List<Book>> getBooksSortedByTitle() {
        return new ResponseEntity<>(bookService.getBooksSortedByTitle(), HttpStatus.OK);
    }

    @GetMapping("/sorted/latest")
    public ResponseEntity<List<Book>> getBooksSortedByLatest() {
        return new ResponseEntity<>(bookService.getBooksSortedByLatest(), HttpStatus.OK);
    }
}