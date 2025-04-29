package com.libraryhub.book.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String description;
    private String fileName;     // Actual PDF file name stored
    private String downloadUrl;  // Link to download
    //private int downloadCount = 0;
}
