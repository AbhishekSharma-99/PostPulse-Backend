package com.postpulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.category.CategoryRequest;
import com.postpulse.payload.category.CategoryResponse;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest extends BaseControllerTest {

    LocalDateTime fixedTimestamp = LocalDateTime.of(2025, 1, 1, 12, 0);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserDetailsService userDetailsService;

    // ==================== CREATE CATEGORY ====================

    @Nested
    @DisplayName("POST /api/v1/categories")
    class CreateCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN with valid payload → 201 Created")
        void createCategory_AdminValidPayload_Returns201() throws Exception {
            // Arrange
            CategoryRequest request = buildCategoryRequest();
            CategoryResponse response = new CategoryResponse(
                    1L,
                    "Technology",
                    "Tech related posts",
                    fixedTimestamp,
                    fixedTimestamp);
            given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Technology"))
                    .andExpect(jsonPath("$.description").value("Tech related posts"));

            verify(categoryService).createCategory(any(CategoryRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden, service not called")
        void createCategory_UserRole_Returns403() throws Exception {
            // Arrange
            CategoryRequest request = buildCategoryRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void createCategory_Unauthenticated_Returns401() throws Exception {
            // Arrange
            CategoryRequest request = buildCategoryRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN with blank category name → 400 Bad Request")
        void createCategory_BlankName_Returns400() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest("", "Some description");

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any());
        }
    }

    // ==================== GET CATEGORY BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/categories/{categoryId}")
    class GetCategoryById {

        @Test
        @DisplayName("Valid ID → 200 OK with CategoryResponse")
        void getCategoryById_ValidId_Returns200() throws Exception {
            // Arrange
            CategoryResponse response = new CategoryResponse(
                    1L,
                    "Technology",
                    "Tech related posts",
                    fixedTimestamp,
                    fixedTimestamp);
            given(categoryService.getCategoryById(1L)).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Technology"));

            verify(categoryService).getCategoryById(1L);
        }

        @Test
        @DisplayName("Non-existent ID → 404 Not Found")
        void getCategoryById_NotFound_Returns404() throws Exception {
            // Arrange
            given(categoryService.getCategoryById(999L)).willThrow(new ResourceNotFoundException(
                    "Category",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/999")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Negative ID → 400 Bad Request (path variable constraint)")
        void getCategoryById_NegativeId_Returns400() throws Exception {
            // Act & Assert — @Positive constraint on path variable rejects -1
            mockMvc.perform(get("/api/v1/categories/-1")).andExpect(status().isBadRequest());

            verify(categoryService, never()).getCategoryById(any());
        }
    }

    // ==================== GET ALL CATEGORIES ====================

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetAllCategories {

        @Test
        @DisplayName("Categories exist → 200 OK with list")
        void getAllCategories_CategoriesExist_Returns200WithList() throws Exception {
            // Arrange
            List<CategoryResponse> categories = List.of(
                    new CategoryResponse(1L, "Technology", "Tech posts", fixedTimestamp, fixedTimestamp),
                    new CategoryResponse(2L, "Finance", "Finance posts", fixedTimestamp, fixedTimestamp));
            given(categoryService.getAllCategories()).willReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Technology"))
                    .andExpect(jsonPath("$[1].name").value("Finance"));
        }

        @Test
        @DisplayName("No categories → 200 OK with empty list")
        void getAllCategories_NoneExist_Returns200WithEmptyList() throws Exception {
            // Arrange
            given(categoryService.getAllCategories()).willReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==================== UPDATE CATEGORY ====================

    @Nested
    @DisplayName("PUT /api/v1/categories/{categoryId}")
    class UpdateCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN with valid payload → 200 OK with updated response")
        void updateCategory_AdminValidPayload_Returns200() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest("Updated Tech", "Updated description");
            CategoryResponse response = new CategoryResponse(
                    1L,
                    "Updated Tech",
                    "Updated description",
                    fixedTimestamp,
                    fixedTimestamp);
            given(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/1")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Tech"));

            verify(categoryService).updateCategory(eq(1L), any(CategoryRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden")
        void updateCategory_UserRole_Returns403() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest("Updated Tech", "Updated description");

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/1")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(categoryService, never()).updateCategory(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Non-existent category → 404 Not Found")
        void updateCategory_NotFound_Returns404() throws Exception {
            // Arrange
            CategoryRequest request = new CategoryRequest("Updated Tech", "Updated description");
            given(categoryService.updateCategory(
                    eq(999L),
                    any(CategoryRequest.class))).willThrow(new ResourceNotFoundException(
                    "Category",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/999")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }


    // ==================== DELETE CATEGORY ====================

    @Nested
    @DisplayName("DELETE /api/v1/categories/{categoryId}")
    class DeleteCategory {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN valid ID → 204 No Content")
        void deleteCategory_AdminValidId_Returns204() throws Exception {
            // Arrange
            willDoNothing().given(categoryService).deleteCategory(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/1")).andExpect(status().isNoContent());

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden")
        void deleteCategory_UserRole_Returns403() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/1")).andExpect(status().isForbidden());

            verify(categoryService, never()).deleteCategory(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Non-existent category → 404 Not Found")
        void deleteCategory_NotFound_Returns404() throws Exception {
            // Arrange
            willThrow(new ResourceNotFoundException("Category", "id", 999L)).given(categoryService)
                    .deleteCategory(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/999")).andExpect(status().isNotFound());
        }
    }

    // ==================== HELPERS ====================

    private CategoryRequest buildCategoryRequest() {
        return new CategoryRequest("Technology", "Tech related posts");
    }

}