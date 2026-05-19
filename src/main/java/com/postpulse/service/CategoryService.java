package com.postpulse.service;

import com.postpulse.payload.category.CategoryRequest;
import com.postpulse.payload.category.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse getCategoryById(Long categoryId);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest);

    void deleteCategory(Long categoryId);
}
