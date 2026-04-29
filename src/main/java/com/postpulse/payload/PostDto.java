package com.postpulse.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Post request payload")
public class PostDto {

    @Schema(description = "Blog post Title")
    @NotBlank(message = "Title is required")
    @Size(min = 2, message = "Post title must be at least 2 characters")
    private String title;

    @Schema(description = "Blog post Description")
    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Post description must be at least 10 characters")
    private String description;

    @Schema(description = "Blog post Content")
    @NotBlank(message = "Content is required")
    private String content;

    @Schema(description = "Blog post Category ID")
    @NotNull(message = "Category id is required")
    private Long categoryId;
}