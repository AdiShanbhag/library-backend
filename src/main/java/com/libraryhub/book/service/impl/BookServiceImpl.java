package com.libraryhub.book.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.libraryhub.book.model.Book;
import com.libraryhub.book.repository.BookRepository;
import com.libraryhub.book.service.BookService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Cloudinary cloudinary;

    private final String uploadDir = "path_to_upload_directory";  // Not needed for Cloudinary, just for local fallback

    // MIME type mapping based on file extension
    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "pdf", "application/pdf",
            "epub", "application/epub+zip",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "txt", "text/plain"
            // Add more types as needed
    );
    @Override
    public Book saveBook(MultipartFile file, String title, String author, String description) throws IOException {
        // Clean the file name
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // ✅ Restrict allowed file types
        String ext = FilenameUtils.getExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = List.of("pdf", "epub", "docx");

        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("Only PDF, EPUB, and DOCX files are allowed.");
        }

        // Convert to snake_case and lower case
        String snakeCaseFileName = originalFilename
                .replaceAll("\\s+", "_")   // replace spaces with underscores
                .replaceAll("[^a-zA-Z0-9._]", "")  // remove special characters except dot and underscore
                .toLowerCase();

        // Check if a book with the same file name already exists
        Optional<Book> existingBook = bookRepository.findByFileName(snakeCaseFileName);
        if (existingBook.isPresent()) {
            throw new FileAlreadyExistsException("The book you are trying to upload, already exists");
        }

        // Upload the file to Cloudinary and get the download URL
        String cloudinaryUrl = uploadToCloudinary(file);

        // Save the book metadata
        Book book = Book.builder()
                .title(title)
                .author(author)
                .description(description)
                .fileName(snakeCaseFileName)
                .downloadUrl(cloudinaryUrl)
                .build();

        return bookRepository.save(book);
    }

    @Override
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public void deleteBook(Long id) throws IOException {
        Book book = getBookById(id);
        if (book != null) {
            // Delete the file from Cloudinary if needed (optional, but could be done)
            cloudinary.uploader().destroy(book.getFileName(), ObjectUtils.emptyMap());

            // Remove book metadata from DB
            bookRepository.delete(book);
        }
    }

    public String uploadToCloudinary(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); // e.g., myfile.pdf

        System.out.println("Filename: " + originalFilename);
        Map uploadParams = ObjectUtils.asMap(
                "resource_type", "raw",
                "use_filename", true,
                "unique_filename", false,
                "folder", "books",  // optional: store in folder
                "flags", "attachment",
                "public_id", originalFilename
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        //System.out.println("Upload result: " + uploadResult);
        String secureUrl = (String) uploadResult.get("secure_url");

        // Replace to make sure download behavior is always enforced
        return secureUrl.replace("/upload/", "/upload/fl_attachment/");
    }

    @Override
    public ResponseEntity<byte[]> downloadBookById(Long id) throws IOException {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Book not found with ID: " + id));

        String fileUrl = URLDecoder.decode(book.getDownloadUrl(), "UTF-8");

        // Fetch file content from Cloudinary
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> cloudResponse = restTemplate.getForEntity(fileUrl, byte[].class);

        if (cloudResponse.getStatusCode() != HttpStatus.OK || cloudResponse.getBody() == null) {
            throw new IOException("Failed to download file from Cloudinary");
        }

        //Determine MIME type from file extension
        String fileName = book.getFileName();
        String contentType = Files.probeContentType(Paths.get(fileName));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        //Correct headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename(fileName)
                .build());
        headers.setContentLength(cloudResponse.getBody().length);

        // Increment the download count
        book.setDownloadCount(book.getDownloadCount() + 1);
        bookRepository.save(book);

        return new ResponseEntity<>(cloudResponse.getBody(), headers, HttpStatus.OK);
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Override
    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> getBooksSortedByTitle() {
        return bookRepository.findAllByOrderByTitleAsc();
    }

    public List<Book> getBooksSortedByLatest() {
        return bookRepository.findAllByOrderByCreatedAtDesc();
    }
}