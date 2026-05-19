package com.postpulse.payload.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating an existing post. " +
        "Slug can be explicitly provided to override the auto-generated value. " +
        "If slug is blank or null, it is regenerated from the updated title.")
public class PostUpdateRequest {

    @Schema(description = "Updated post title.", example = "Getting Started with Spring Boot 3 — Revised")
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Schema(description = "Updated short description.", example = "A revised, deeper guide to Spring Boot 3")
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Schema(description = "Updated post body.", example = "Spring Boot 3 ships with native image support via GraalVM...")
    @NotBlank(message = "Content is required")
    @Size(min = 20, message = "Content must be at least 20 characters")
    private String content;

    @Schema(description = "ID of the category to assign this post to.", example = "3")
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be a positive number")
    private Long categoryId;
}