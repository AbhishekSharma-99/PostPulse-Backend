package com.springboot.blog.payload;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        description = "CategoryDto Model Information"
)
public class CategoryDto {
    @Schema(description = "Category ID")
    private Long id;
    @Schema(description = "Category Name")
    private String name;
    @Schema(description = "Category Description")
    private String description;

}
