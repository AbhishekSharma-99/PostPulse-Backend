package com.springboot.blog.controller;

import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(
        name = "Category API",
        description = "CRUD REST APIs for Category Resource"
)
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Add a new category",
            description = "This API is used to add a new category.",
            tags = {"Category API"}
    )
    @ApiResponse(
            responseCode = "201",
            description = "Category added successfully"
    )
    @SecurityRequirement(
            name = "BearerAuth"
    )
    // build Add category REST API
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> addCategory(@RequestBody CategoryDto categoryDto) {
        CategoryDto response = categoryService.addCategory(categoryDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get category by ID",
            description = "This API is used to get category by ID.",
            tags = {"Category API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Category retrieved successfully"
    )
    // build Get category by ID REST API
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable long categoryId) {
        CategoryDto response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all categories",
            description = "This API is used to get all categories.",
            tags = {"Category API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully"
    )
    // build Get ALL category REST API
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update a category",
            description = "This API is used to update a category.",
            tags = {"Category API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Category updated successfully"
    )
    @SecurityRequirement(
            name = "BearerAuth"
    )
    // build Update category REST API
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable long categoryId, @RequestBody CategoryDto categoryDto) {
        CategoryDto response = categoryService.updateCategory(categoryId, categoryDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a category",
            description = "This API is used to delete a category.",
            tags = {"Category API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Category deleted successfully"
    )
    @SecurityRequirement(
            name = "BearerAuth"
    )
    //build Delete category REST API
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully");
    }
}
