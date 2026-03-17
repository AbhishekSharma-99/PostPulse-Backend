package com.postpulse.repository;

import com.postpulse.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveCategory_ShouldPersistCategory() {
        // Given
        Category category = new Category();
        category.setName("Technology");
        category.setDescription("Tech posts and articles");

        // When
        Category savedCategory = categoryRepository.save(category);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isGreaterThan(0);
        assertThat(savedCategory.getName()).isEqualTo("Technology");
    }

    @Test
    void findById_ShouldReturnCategory_WhenExists() {
        // Given
        Category category = new Category();
        category.setName("Technology");
        category.setDescription("Tech posts and articles");
        Category savedCategory = categoryRepository.save(category);

        // When
        var foundCategory = categoryRepository.findById(savedCategory.getId());

        // Then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Technology");
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Given
        Category category1 = new Category();
        category1.setName("Technology");
        category1.setDescription("Tech posts");
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setName("Sports");
        category2.setDescription("Sports posts");
        categoryRepository.save(category2);

        // When
        var allCategories = categoryRepository.findAll();

        // Then
        assertThat(allCategories).hasSize(2);
        assertThat(allCategories).extracting("name").contains("Technology", "Sports");
    }

    @Test
    void updateCategory_ShouldUpdateCategoryData() {
        // Given
        Category category = new Category();
        category.setName("Technology");
        category.setDescription("Tech posts");
        Category savedCategory = categoryRepository.save(category);

        // When
        savedCategory.setName("Updated Technology");
        Category updatedCategory = categoryRepository.save(savedCategory);

        // Then
        assertThat(updatedCategory.getName()).isEqualTo("Updated Technology");
    }

    @Test
    void deleteCategory_ShouldRemoveCategory() {
        // Given
        Category category = new Category();
        category.setName("Technology");
        category.setDescription("Tech posts");
        Category savedCategory = categoryRepository.save(category);

        // When
        categoryRepository.delete(savedCategory);
        var deletedCategory = categoryRepository.findById(savedCategory.getId());

        // Then
        assertThat(deletedCategory).isEmpty();
    }
}
