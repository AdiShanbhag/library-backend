package com.libraryhub.book.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public class PdfMetadataExtractor {

    public static ExtractedMetadata extractMetadata(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDDocumentInformation info = document.getDocumentInformation();

            String title = Optional.ofNullable(info.getTitle()).orElse("");
            String author = Optional.ofNullable(info.getAuthor()).orElse("");
            String description = Optional.ofNullable(info.getSubject()).orElse("");

            if (title.isBlank() || author.isBlank() || description.isBlank()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(1);
                String firstPageText = stripper.getText(document).strip();

                if (title.isBlank()) title = guessTitle(firstPageText);
                if (author.isBlank()) author = guessAuthor(firstPageText);
                if (description.isBlank()) description = summarize(firstPageText);
            }

            return ExtractedMetadata.builder()
                    .title(title)
                    .author(author)
                    .description(description)
                    .build();
        }
    }

    private static String guessTitle(String text) {
        // You can improve this logic as needed
        return text.lines().findFirst().orElse("Untitled");
    }

    private static String guessAuthor(String text) {
        return "Unknown"; // Or extract by pattern (e.g., "By John Doe")
    }

    private static String summarize(String text) {
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtractedMetadata {
        private String title;
        private String author;
        private String description;
    }
}