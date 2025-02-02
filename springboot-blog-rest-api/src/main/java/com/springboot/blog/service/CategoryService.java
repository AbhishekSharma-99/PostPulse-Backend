package com.springboot.blog.service;

import com.springboot.blog.payload.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CategoryDto categoryDto);

    CategoryDto getCategoryById(long categoryId);

    List<CategoryDto> getAllCategories();

    CategoryDto updateCategory(long categoryId, CategoryDto categoryDto);

    void deleteCategory(long categoryId);
}
