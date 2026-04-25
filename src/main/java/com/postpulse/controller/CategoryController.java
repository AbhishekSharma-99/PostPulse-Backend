package com.postpulse.controller;

import com.postpulse.payload.CategoryRequest;
import com.postpulse.payload.CategoryResponse;
import com.postpulse.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category API", description = "CRUD REST APIs for Category Resource")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Add a new category", description = "Add a new category. ADMIN only.")
    @ApiResponse(responseCode = "201", description = "Category added successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> addCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.addCategory(categoryRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID.")
    @ApiResponse(responseCode = "200", description = "Category retrieved successfully")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @Operation(summary = "Get all categories", description = "Retrieve all categories.")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Update a category", description = "Update an existing category. ADMIN only.")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable long categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, categoryRequest));
    }

    @Operation(summary = "Delete a category", description = "Delete a category by ID. ADMIN only.")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}