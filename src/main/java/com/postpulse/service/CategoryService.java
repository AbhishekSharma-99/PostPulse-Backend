package com.postpulse.service;

import com.postpulse.payload.CategoryRequest;
import com.postpulse.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse addCategory(CategoryRequest categoryRequest);

    CategoryResponse getCategoryById(long categoryId);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(long categoryId, CategoryRequest categoryRequest);

    void deleteCategory(long categoryId);
}
