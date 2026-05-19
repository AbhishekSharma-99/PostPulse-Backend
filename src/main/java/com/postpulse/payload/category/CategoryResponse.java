package com.postpulse.payload.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Category detail response. Returned after successful creation or when fetching category info.")
public class CategoryResponse {

    @Schema(description = "Unique identifier of the category", example = "5")
    private Long id;

    @Schema(description = "Category name (unique)", example = "Technology")
    private String name;

    @Schema(description = "Optional category description", example = "All posts related to modern software development")
    private String description;

    @Schema(description = "Timestamp when the category was created (UTC)")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update (UTC)")
    private LocalDateTime updatedAt;
}