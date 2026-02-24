package com.example.auth.book.dto;

import com.example.auth.book.entity.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class BookDto {

    private BookDto() {
    }

    public record CreateRequest(
            @NotBlank String title,
            @NotBlank String author,
            @NotNull Long categoryId
    ) {
    }

    public record UpdateRequest(
            @NotBlank String title,
            @NotBlank String author,
            @NotNull Long categoryId
    ) {
    }

    public record Response(
            Long id,
            String title,
            String author,
            Long categoryId,
            String categoryName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(Book book) {
            String categoryName = book.getCategory() != null ? book.getCategory().getName() : null;
            return new Response(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategoryId(),
                    categoryName,
                    book.getCreatedAt(),
                    book.getUpdatedAt()
            );
        }
    }
}
