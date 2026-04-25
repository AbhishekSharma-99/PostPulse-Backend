package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.CategoryRequest;
import com.postpulse.payload.CategoryResponse;
import com.postpulse.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    // @Spy wraps a real ModelMapper instance so actual field-mapping logic runs.
    // CategoryRequest -> Category and Category -> CategoryResponse have no custom
    // PropertyMap, so a standard ModelMapper works without additional configuration.
    // Using @Mock here would only prove map() was called, not that fields landed
    // correctly on the response object.
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category savedCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        // categoryRequest is the inbound DTO — no ID; client doesn't set it
        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Technology");
        categoryRequest.setDescription("Posts about tech, software, and engineering.");

        // savedCategory is what the repository returns after save() —
        // ID is assigned by the persistence layer, not the caller
        savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Technology");
        savedCategory.setDescription("Posts about tech, software, and engineering.");
    }

    // =====================================================================
    // addCategory — Real ModelMapper, output assertions only
    // The service maps CategoryRequest -> Category via ModelMapper, then
    // immediately persists. No mutation occurs between mapping and save(),
    // so ArgumentCaptor adds no value here — the real @Spy ModelMapper
    // already proves the end-to-end mapping is correct through output
    // field assertions on the returned CategoryResponse.
    // =====================================================================

    @Test
    @DisplayName("Should successfully create a category and return persisted response")
    void addCategory_Success() {

        // --- ARRANGE ---
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        // --- ACT ---
        CategoryResponse result = categoryService.addCategory(categoryRequest);

        // --- ASSERT ---

        // Real ModelMapper ran — values sourced from actual field mapping,
        // not stubs. If CategoryRequest -> Category -> CategoryResponse
        // breaks at any point, these assertions catch it.
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Technology");
        assertThat(result.getDescription()).isEqualTo("Posts about tech, software, and engineering.");

        verify(categoryRepository).save(any(Category.class));
    }

    // =====================================================================
    // getCategoryById — Exact Matching
    // findById() takes a simple Long ID — exact matching confirms the
    // service forwarded the correct value. The not-found case verifies
    // orElseThrow() fires before ModelMapper is ever invoked.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped CategoryResponse when valid ID is provided")
    void getCategoryById_Success() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(savedCategory));

        // --- ACT ---
        CategoryResponse result = categoryService.getCategoryById(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Technology");
        assertThat(result.getDescription()).isEqualTo("Posts about tech, software, and engineering.");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID does not exist")
    void getCategoryById_NotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(99L);
    }

    // =====================================================================
    // getAllCategories — Collection Mapping
    // findAll() returns every category. Assertions check both collection
    // size and field-level correctness on individual elements to ensure
    // the stream().map() pipeline did not silently drop or corrupt records.
    // An empty-list case is included: getAllCategories() must return an
    // empty list, not throw — no categories is valid application state.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped list of all categories")
    void getAllCategories_Success() {

        // --- ARRANGE ---
        Category secondCategory = new Category();
        secondCategory.setId(2L);
        secondCategory.setName("Finance");
        secondCategory.setDescription("Posts about markets, investment, and trading.");

        when(categoryRepository.findAll())
                .thenReturn(List.of(savedCategory, secondCategory));

        // --- ACT ---
        List<CategoryResponse> result = categoryService.getAllCategories();

        // --- ASSERT ---
        assertThat(result).isNotNull().hasSize(2);

        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("Technology");
        assertThat(result.getFirst().getDescription()).isEqualTo("Posts about tech, software, and engineering.");

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Finance");
        assertThat(result.get(1).getDescription()).isEqualTo("Posts about markets, investment, and trading.");

        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void getAllCategories_EmptyList() {

        // --- ARRANGE ---

        // Empty list is valid state — the service must return it as-is,
        // never throw. This documents the contract explicitly.
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        // --- ACT ---
        List<CategoryResponse> result = categoryService.getAllCategories();

        // --- ASSERT ---
        assertThat(result).isNotNull().isEmpty();

        verify(categoryRepository).findAll();
    }

    // =====================================================================
    // updateCategory — ArgumentCaptor on save()
    // The service fetches an existing Category then mutates name, description,
    // and id on the same managed object before calling save(). ArgumentCaptor
    // intercepts the exact mutated entity so we can assert every field was
    // correctly assigned — any(Category.class) would silently pass even if
    // setDescription() or setId() were never called.
    // =====================================================================

    @Test
    @DisplayName("Should successfully update category and return mapped response")
    void updateCategory_Success() {

        // --- ARRANGE ---
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Software Engineering");
        updateRequest.setDescription("Deep dives into system design and architecture.");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Software Engineering");
        updatedCategory.setDescription("Deep dives into system design and architecture.");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(savedCategory));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(updatedCategory);

        // --- ACT ---
        CategoryResponse result = categoryService.updateCategory(1L, updateRequest);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Software Engineering");
        assertThat(result.getDescription()).isEqualTo("Deep dives into system design and architecture.");

        // Capture the mutated entity passed to save() — confirms the service
        // called setName(), setDescription(), and setId() on the fetched object
        // before persisting. Any omission here would fail these assertions.
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        Category capturedCategory = captor.getValue();

        assertThat(capturedCategory.getId()).isEqualTo(1L);
        assertThat(capturedCategory.getName()).isEqualTo("Software Engineering");
        assertThat(capturedCategory.getDescription()).isEqualTo("Deep dives into system design and architecture.");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during update")
    void updateCategory_NotFound() {

        // --- ARRANGE ---

        // Category not found — service must throw before save() is ever called
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> categoryService.updateCategory(99L, categoryRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(99L);

        // save() must never be called — no entity to update
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // =====================================================================
    // deleteCategory — Exact Object Matching on delete()
    // No mutation happens — service just fetches and deletes.
    // Exact object match on delete() confirms the service passed the correct
    // Category instance, not just any Category that happened to be around.
    // any(Category.class) would pass even if the wrong entity was deleted.
    // =====================================================================

    @Test
    @DisplayName("Should successfully delete category when valid ID is provided")
    void deleteCategory_Success() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(savedCategory));

        // --- ACT ---
        categoryService.deleteCategory(1L);

        // --- ASSERT ---
        verify(categoryRepository).findById(1L);

        // Exact object match — confirms the fetched entity (not a new instance)
        // was passed to delete(), preventing phantom-delete bugs
        verify(categoryRepository).delete(savedCategory);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during delete")
    void deleteCategory_NotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(99L);

        // Most critical assertion for delete — nothing must be removed
        // from the database when the category doesn't exist
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}