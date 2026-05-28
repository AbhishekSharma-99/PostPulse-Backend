package com.postpulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.post.*;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.PostService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@DisplayName("PostController Tests")
class PostControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    // ==================== CREATE POST ====================

    @Nested
    @DisplayName("POST /api/v1/posts")
    class CreatePost {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN with valid payload → 201 Created")
        void createPost_AdminValidPayload_Returns201() throws Exception {
            // Arrange
            PostCreateRequest request = buildCreateRequest();
            PostResponse response = buildPostResponse(1L, "Spring Boot Internals", "spring-boot-internals");
            given(postService.createPost(any(PostCreateRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot Internals"))
                    .andExpect(jsonPath("$.slug").value("spring-boot-internals"));

            verify(postService).createPost(any(PostCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden")
        void createPost_UserRole_Returns403() throws Exception {
            // Arrange
            PostCreateRequest request = buildCreateRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(postService, never()).createPost(any());
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void createPost_Unauthenticated_Returns401() throws Exception {
            // Arrange
            PostCreateRequest request = buildCreateRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(postService, never()).createPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Blank title → 400 Bad Request")
        void createPost_BlankTitle_Returns400() throws Exception {
            // Arrange — title is mandatory per Bean Validation
            PostCreateRequest request = new PostCreateRequest(
                    "",
                    "Valid description here",
                    "Valid content with enough length",
                    1L);

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(postService, never()).createPost(any());
        }
    }

    // ==================== GET ALL POSTS ====================

    @Nested
    @DisplayName("GET /api/v1/posts")
    class GetAllPosts {

        @Test
        @DisplayName("Default pagination params → 200 OK with PostPageResponse")
        void getAllPosts_DefaultParams_Returns200() throws Exception {
            // Arrange
            PostPageResponse pageResponse = buildPageResponse(
                    List.of(buildPostSummary(1L, "Post One", "post-one"), buildPostSummary(2L, "Post Two", "post-two")),
                    0,
                    10,
                    2L,
                    1,
                    true);
            given(postService.getAllPosts(anyInt(), anyInt(), anyString(), anyString())).willReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.pageNo").value(0))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("Custom pagination params → 200 OK, params forwarded to service")
        void getAllPosts_CustomParams_Returns200() throws Exception {
            // Arrange
            PostPageResponse pageResponse = buildPageResponse(List.of(), 1, 5, 0L, 0, true);
            given(postService.getAllPosts(1, 5, "title", "asc")).willReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts").param("pageNo", "1")
                    .param("pageSize", "5")
                    .param("sortBy", "title")
                    .param("sortDir", "asc")).andExpect(status().isOk());

            verify(postService).getAllPosts(1, 5, "title", "asc");
        }

        @Test
        @DisplayName("No posts exist → 200 OK with empty content list")
        void getAllPosts_NoPosts_Returns200WithEmptyContent() throws Exception {
            // Arrange
            PostPageResponse pageResponse = buildPageResponse(List.of(), 0, 10, 0L, 0, true);
            given(postService.getAllPosts(anyInt(), anyInt(), anyString(), anyString())).willReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    // ==================== GET POST BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/posts/{id}")
    class GetPostById {

        @Test
        @DisplayName("Valid ID → 200 OK with PostResponse")
        void getPostById_ValidId_Returns200() throws Exception {
            // Arrange
            PostResponse response = buildPostResponse(1L, "Spring Boot Internals", "spring-boot-internals");
            given(postService.getPostById(1L)).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Spring Boot Internals"));
        }

        @Test
        @DisplayName("Non-existent ID → 404 Not Found")
        void getPostById_NotFound_Returns404() throws Exception {
            // Arrange
            given(postService.getPostById(999L)).willThrow(new ResourceNotFoundException("Post", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/999")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Negative ID → 400 Bad Request (@Positive constraint)")
        void getPostById_NegativeId_Returns400() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/-1")).andExpect(status().isBadRequest());

            verify(postService, never()).getPostById(any());
        }
    }

    // ==================== UPDATE POST ====================

    @Nested
    @DisplayName("PUT /api/v1/posts/{id}")
    class UpdatePost {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN with valid payload → 200 OK with updated PostResponse")
        void updatePost_AdminValidPayload_Returns200() throws Exception {
            // Arrange
            PostUpdateRequest request = buildUpdateRequest();
            PostResponse response = buildPostResponse(1L, "Updated Title", "updated-title");
            given(postService.updatePost(any(PostUpdateRequest.class), eq(1L))).willReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));

            verify(postService).updatePost(any(PostUpdateRequest.class), eq(1L));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden")
        void updatePost_UserRole_Returns403() throws Exception {
            // Arrange
            PostUpdateRequest request = buildUpdateRequest();

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(postService, never()).updatePost(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Non-existent post → 404 Not Found")
        void updatePost_NotFound_Returns404() throws Exception {
            // Arrange
            PostUpdateRequest request = buildUpdateRequest();
            given(postService.updatePost(
                    any(PostUpdateRequest.class),
                    eq(999L))).willThrow(new ResourceNotFoundException(
                    "Post",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/999")

                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== DELETE POST ====================

    @Nested
    @DisplayName("DELETE /api/v1/posts/{id}")
    class DeletePost {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN valid ID → 204 No Content")
        void deletePost_AdminValidId_Returns204() throws Exception {
            // Arrange
            willDoNothing().given(postService).deletePost(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1")).andExpect(status().isNoContent());

            verify(postService).deletePost(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("USER role → 403 Forbidden")
        void deletePost_UserRole_Returns403() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1")).andExpect(status().isForbidden());

            verify(postService, never()).deletePost(any());
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void deletePost_Unauthenticated_Returns401() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1")).andExpect(status().isUnauthorized());

            verify(postService, never()).deletePost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Non-existent post → 404 Not Found")
        void deletePost_NotFound_Returns404() throws Exception {
            // Arrange
            willThrow(new ResourceNotFoundException("Post", "id", 999L)).given(postService).deletePost(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/999")).andExpect(status().isNotFound());
        }
    }

    // ==================== GET POSTS BY CATEGORY ====================

    @Nested
    @DisplayName("GET /api/v1/posts/category/{categoryId}")
    class GetPostsByCategory {

        @Test
        @DisplayName("Valid category ID → 200 OK with post summary list")
        void getPostsByCategory_ValidCategoryId_Returns200() throws Exception {
            // Arrange
            List<PostSummary> summaries = List.of(
                    buildPostSummary(1L, "Post One", "post-one"),
                    buildPostSummary(2L, "Post Two", "post-two"));
            given(postService.getPostsByCategory(1L)).willReturn(summaries);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("Post One"));
        }

        @Test
        @DisplayName("Category not found → 404 Not Found")
        void getPostsByCategory_CategoryNotFound_Returns404() throws Exception {
            // Arrange
            given(postService.getPostsByCategory(999L)).willThrow(new ResourceNotFoundException(
                    "Category",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/category/999")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Category has no posts → 200 OK with empty list")
        void getPostsByCategory_NoPosts_Returns200WithEmptyList() throws Exception {
            // Arrange
            given(postService.getPostsByCategory(1L)).willReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ==================== GET POST BY SLUG ====================

    @Nested
    @DisplayName("GET /api/v1/posts/slug/{slug}")
    class GetPostBySlug {

        @Test
        @DisplayName("Valid slug → 200 OK with PostResponse")
        void getPostBySlug_ValidSlug_Returns200() throws Exception {
            // Arrange
            PostResponse response = buildPostResponse(1L, "Spring Boot Internals", "spring-boot-internals");
            given(postService.getPostBySlug("spring-boot-internals")).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/slug/spring-boot-internals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slug").value("spring-boot-internals"))
                    .andExpect(jsonPath("$.title").value("Spring Boot Internals"));
        }

        @Test
        @DisplayName("Non-existent slug → 404 Not Found")
        void getPostBySlug_NotFound_Returns404() throws Exception {
            // Arrange
            given(postService.getPostBySlug("non-existent-slug")).willThrow(new ResourceNotFoundException(
                    "Post",
                    "slug",
                    "non-existent-slug"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/slug/non-existent-slug")).andExpect(status().isNotFound());
        }
    }

    // ==================== HELPERS ====================

    private PostCreateRequest buildCreateRequest() {
        return new PostCreateRequest(
                "Spring Boot Internals",
                "Deep dive content",
                "Deep dive content into Spring Boot internals",
                1L);
    }

    private PostUpdateRequest buildUpdateRequest() {
        return new PostUpdateRequest("Updated Title", "Updated description", "Updated content with enough length", 1L);
    }

    private PostResponse buildPostResponse(Long id, String title, String slug) {
        PostResponse response = new PostResponse();
        response.setId(id);
        response.setTitle(title);
        response.setSlug(slug);
        return response;
    }

    private PostSummary buildPostSummary(Long id, String title, String slug) {
        PostSummary summary = new PostSummary();
        summary.setId(id);
        summary.setTitle(title);
        summary.setSlug(slug);
        return summary;
    }

    private PostPageResponse buildPageResponse(
            List<PostSummary> content,
            int pageNo,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean last) {
        PostPageResponse response = new PostPageResponse();
        response.setContent(content);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setLast(last);
        return response;
    }
}