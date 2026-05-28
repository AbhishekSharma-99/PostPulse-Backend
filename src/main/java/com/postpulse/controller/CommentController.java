package com.postpulse.controller;

import com.postpulse.annotation.CommonApiResponses;
import com.postpulse.payload.comment.CommentRequest;
import com.postpulse.payload.comment.CommentResponse;
import com.postpulse.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Comment API", description = "CRUD REST APIs for Comment Resource")
@CommonApiResponses
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a comment", description = "Create a new comment for a specific post.")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable @NotNull @Positive Long postId,
            @Valid @RequestBody CommentRequest commentRequest) {
        return new ResponseEntity<>(commentService.createComment(postId, commentRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all comments", description = "Get all comments for a specific post.")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable @NotNull @Positive Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @Operation(summary = "Get comment by ID", description = "Get a specific comment by ID.")
    @ApiResponse(responseCode = "200", description = "Comment retrieved successfully")
    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable @NotNull @Positive Long postId,
            @PathVariable @NotNull @Positive Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(postId, commentId));
    }

    @Operation(summary = "Update a comment", description = "Update a specific comment by ID.")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateCommentById(
            @PathVariable @NotNull @Positive Long postId,
            @PathVariable @NotNull @Positive Long commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        return ResponseEntity.ok(commentService.updateCommentById(postId, commentId, commentRequest));
    }

    @Operation(summary = "Delete a comment", description = "Delete a specific comment by ID.")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentById(
            @PathVariable @NotNull @Positive Long postId,
            @PathVariable @NotNull @Positive Long commentId) {
        commentService.deleteCommentById(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}