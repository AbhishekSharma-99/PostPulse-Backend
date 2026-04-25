package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.CategoryRequest;
import com.postpulse.payload.CategoryResponse;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest categoryRequest) {
        Category category = modelMapper.map(categoryRequest, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Override
    public CategoryResponse getCategoryById(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(long categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setId(categoryId);

        return modelMapper.map(categoryRepository.save(category), CategoryResponse.class);
    }

    @Override
    @Transactional
    public void deleteCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        categoryRepository.delete(category);
    }
}