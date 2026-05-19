package com.postpulse.payload.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new post. " +
        "Slug is auto-generated from title. Author is resolved from the security context.")
public class PostCreateRequest {

    @Schema(description = "Post title. Must be unique across all posts.", example = "Getting Started with Spring Boot 3")
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Schema(description = "Short description shown in post listings and SEO meta.", example = "A practical guide to building production-ready APIs")
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Schema(description = "Full post body. Markdown or plain text.", example = "Spring Boot 3 requires Java 17 as a baseline...")
    @NotBlank(message = "Content is required")
    @Size(min = 20, message = "Content must be at least 20 characters")
    private String content;

    @Schema(description = "ID of the category this post belongs to.", example = "3")
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be a positive number")
    private Long categoryId;
}