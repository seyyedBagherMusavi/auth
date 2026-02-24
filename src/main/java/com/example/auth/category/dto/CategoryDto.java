package com.example.auth.category.dto;

import com.example.auth.category.entity.Category;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class CategoryDto {

    private CategoryDto() {
    }

    public record CreateRequest(@NotBlank String name) {
    }

    public record UpdateRequest(@NotBlank String name) {
    }

    public record Response(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(Category category) {
            return new Response(
                    category.getId(),
                    category.getName(),
                    category.getCreatedAt(),
                    category.getUpdatedAt()
            );
        }
    }
}
