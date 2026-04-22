package com.postpulse.payload;

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
@Schema(description = "CategoryDto Model Information")
public class CategoryDto {

    @Schema(description = "Category ID")
    private Long id;

    @Schema(description = "Category Name")
    @NotBlank(message = "Category name is required")
    @Size(min = 2, message = "Category name must be at least 2 characters")
    private String name;

    @Schema(description = "Category Description")
    @NotBlank(message = "Category description is required")
    private String description;
}