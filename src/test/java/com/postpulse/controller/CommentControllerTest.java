package com.postpulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.comment.CommentRequest;
import com.postpulse.payload.comment.CommentResponse;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@DisplayName("CommentController Tests")
class CommentControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    // ==================== CREATE COMMENT ====================

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/comments")
    class CreateComment {

        @Test
        @WithMockUser
        @DisplayName("Authenticated user with valid payload → 201 Created")
        void createComment_AuthenticatedValidPayload_Returns201() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("Great post about Spring Boot!");
            CommentResponse response = buildCommentResponse(1L, "Great post about Spring Boot!", "abhishek");
            given(commentService.createComment(eq(1L), any(CommentRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts/1/comments").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.body").value("Great post about Spring Boot!"))
                    .andExpect(jsonPath("$.userName").value("abhishek"));

            verify(commentService).createComment(eq(1L), any(CommentRequest.class));
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void createComment_Unauthenticated_Returns401() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("Great post!");

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts/1/comments").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized());

            verify(commentService, never()).createComment(any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Blank comment body → 400 Bad Request, service not called")
        void createComment_BlankBody_Returns400() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts/1/comments").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(commentService, never()).createComment(any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Post not found → 404 Not Found")
        void createComment_PostNotFound_Returns404() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("Valid body");
            given(commentService.createComment(
                    eq(999L),
                    any(CommentRequest.class))).willThrow(new ResourceNotFoundException("Post", "id", 999L));

            // Act & Assert
            mockMvc.perform(post("/api/v1/posts/999/comments").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());
        }
    }

    // ==================== GET ALL COMMENTS FOR POST ====================

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}/comments")
    class GetCommentsByPostId {

        @Test
        @DisplayName("Valid post ID → 200 OK with comment list")
        void getCommentsByPostId_ValidPostId_Returns200WithList() throws Exception {
            // Arrange
            List<CommentResponse> comments = List.of(
                    buildCommentResponse(1L, "First comment", "alice"),
                    buildCommentResponse(2L, "Second comment", "bob"));
            given(commentService.getCommentsByPostId(1L)).willReturn(comments);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].body").value("First comment"))
                    .andExpect(jsonPath("$[1].userName").value("bob"));
        }

        @Test
        @DisplayName("Post with no comments → 200 OK with empty list")
        void getCommentsByPostId_NoComments_Returns200WithEmptyList() throws Exception {
            // Arrange
            given(commentService.getCommentsByPostId(1L)).willReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Post not found → 404 Not Found")
        void getCommentsByPostId_PostNotFound_Returns404() throws Exception {
            // Arrange
            given(commentService.getCommentsByPostId(999L)).willThrow(new ResourceNotFoundException(
                    "Post",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/999/comments")).andExpect(status().isNotFound());
        }
    }

    // ==================== GET COMMENT BY ID ====================

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}/comments/{commentId}")
    class GetCommentById {

        @Test
        @DisplayName("Valid post + comment IDs → 200 OK")
        void getCommentById_ValidIds_Returns200() throws Exception {
            // Arrange
            CommentResponse response = buildCommentResponse(1L, "Valid comment", "abhishek");
            given(commentService.getCommentById(1L, 1L)).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1/comments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.body").value("Valid comment"));
        }

        @Test
        @DisplayName("Comment belongs to different post → 400 Bad Request")
        void getCommentById_CommentNotBelongingToPost_Returns400() throws Exception {
            // Arrange — comment 5 exists but belongs to post 2, not post 1
            given(commentService.getCommentById(1L, 5L)).willThrow(new BlogAPIException(
                    HttpStatus.BAD_REQUEST,
                    "Comment with id 5 does not belong to post with id 1"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1/comments/5")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Comment not found → 404 Not Found")
        void getCommentById_CommentNotFound_Returns404() throws Exception {
            // Arrange
            given(commentService.getCommentById(1L, 999L)).willThrow(new ResourceNotFoundException(
                    "Comment",
                    "id",
                    999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/posts/1/comments/999")).andExpect(status().isNotFound());
        }
    }

    // ==================== UPDATE COMMENT ====================

    @Nested
    @DisplayName("PUT /api/v1/posts/{postId}/comments/{commentId}")
    class UpdateComment {

        @Test
        @WithMockUser
        @DisplayName("Owner updates own comment → 200 OK")
        void updateComment_Owner_Returns200() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("Updated comment body");
            CommentResponse response = buildCommentResponse(1L, "Updated comment body", "abhishek");
            given(commentService.updateCommentById(eq(1L), eq(1L), any(CommentRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1/comments/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.body").value("Updated comment body"));

            verify(commentService).updateCommentById(eq(1L), eq(1L), any(CommentRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Non-owner tries to update → 403 Forbidden (service throws)")
        void updateComment_NonOwner_Returns403() throws Exception {
            // Arrange — service enforces ownership via validateCommentBelongsToUser
            CommentRequest request = new CommentRequest("Trying to hijack");
            given(commentService.updateCommentById(
                    eq(1L),
                    eq(2L),
                    any(CommentRequest.class))).willThrow(new BlogAPIException(
                    HttpStatus.FORBIDDEN,
                    "User is not authorized to modify comment with id 2"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1/comments/2").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void updateComment_Unauthenticated_Returns401() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("Some update");

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1/comments/1").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized());

            verify(commentService, never()).updateCommentById(any(), any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Blank update body → 400 Bad Request")
        void updateComment_BlankBody_Returns400() throws Exception {
            // Arrange
            CommentRequest request = new CommentRequest("");

            // Act & Assert
            mockMvc.perform(put("/api/v1/posts/1/comments/1").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(commentService, never()).updateCommentById(any(), any(), any());
        }
    }

    // ==================== DELETE COMMENT ====================

    @Nested
    @DisplayName("DELETE /api/v1/posts/{postId}/comments/{commentId}")
    class DeleteComment {

        @Test
        @WithMockUser
        @DisplayName("Owner deletes own comment → 204 No Content")
        void deleteComment_Owner_Returns204() throws Exception {
            // Arrange
            willDoNothing().given(commentService).deleteCommentById(1L, 1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1/comments/1").with(csrf())).andExpect(status().isNoContent());

            verify(commentService).deleteCommentById(1L, 1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN deletes any comment → 204 No Content")
        void deleteComment_Admin_Returns204() throws Exception {
            // Arrange — ADMIN bypasses ownership check at service level
            willDoNothing().given(commentService).deleteCommentById(1L, 3L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1/comments/3").with(csrf())).andExpect(status().isNoContent());

            verify(commentService).deleteCommentById(1L, 3L);
        }

        @Test
        @WithMockUser
        @DisplayName("Non-owner delete → 403 Forbidden (service throws)")
        void deleteComment_NonOwner_Returns403() throws Exception {
            // Arrange
            willThrow(new BlogAPIException(
                    HttpStatus.FORBIDDEN,
                    "User is not authorized to modify comment with id 2")).given(commentService)
                    .deleteCommentById(1L, 2L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1/comments/2").with(csrf())).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated request → 401 Unauthorized")
        void deleteComment_Unauthenticated_Returns401() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/posts/1/comments/1").with(csrf())).andExpect(status().isUnauthorized());

            verify(commentService, never()).deleteCommentById(any(), any());
        }
    }

    // ==================== HELPERS ====================

    private CommentResponse buildCommentResponse(Long id, String body, String userName) {
        CommentResponse response = new CommentResponse();
        response.setId(id);
        response.setBody(body);
        response.setUserName(userName);
        return response;
    }
}