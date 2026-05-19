package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.category.CategoryRequest;
import com.postpulse.payload.category.CategoryResponse;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
    public CategoryResponse createCategory(CategoryRequest request) {
        validateUniqueName(request.getName(), null);
        Category category = modelMapper.map(request, Category.class);
        Category saved = categoryRepository.save(category);
        log.info("Created category with id: {}", saved.getId());
        return modelMapper.map(saved, CategoryResponse.class);
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
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
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        validateUniqueName(request.getName(), categoryId);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category updated = categoryRepository.save(category);
        log.info("Updated category id: {}", updated.getId());
        return modelMapper.map(updated, CategoryResponse.class);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        categoryRepository.delete(category);
        log.info("Deleted category id: {}", categoryId);
    }

    private void validateUniqueName(String name, Long excludeId) {
        boolean exists = (excludeId == null)
                ? categoryRepository.existsByName(name)
                : categoryRepository.existsByNameAndIdNot(name, excludeId);

        if (exists) {
            throw new BlogAPIException(HttpStatus.CONFLICT, "Category name already exists: " + name);
        }
    }
}