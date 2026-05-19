package com.postpulse.payload.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request payload for creating or updating a category.")
public class CategoryRequest {

    @Schema(description = "Unique category name. Must be between 2 and 100 characters.", example = "Technology")
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Optional description of the category. Maximum 500 characters.",
            example = "All posts related to modern software development")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}