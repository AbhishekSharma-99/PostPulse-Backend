package com.postpulse.controller;

import com.postpulse.payload.CommentDto;
import com.postpulse.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Comment API", description = "CRUD REST APIs for Comment Resource")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a comment", description = "Create a new comment for a specific post.")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable long postId,
            @Valid @RequestBody CommentDto commentDto) {
        return new ResponseEntity<>(commentService.createComment(postId, commentDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all comments", description = "Get all comments for a specific post.")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable long postId) {
        return ResponseEntity.ok(commentService.getByPostId(postId));
    }

    @Operation(summary = "Get comment by ID", description = "Get a specific comment by ID.")
    @ApiResponse(responseCode = "200", description = "Comment retrieved successfully")
    @GetMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(
            @PathVariable long postId,
            @PathVariable long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(postId, commentId));
    }

    @Operation(summary = "Update a comment", description = "Update a specific comment by ID.")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateCommentById(
            @PathVariable long postId,
            @PathVariable long commentId,
            @Valid @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok(commentService.updateCommentById(postId, commentId, commentDto));
    }

    @Operation(summary = "Delete a comment", description = "Delete a specific comment by ID.")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentById(
            @PathVariable long postId,
            @PathVariable long commentId) {
        commentService.deleteCommentById(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}