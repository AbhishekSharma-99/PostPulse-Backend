package com.springboot.blog.controller;

import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@Tag(
        name = "Comment API",
        description = "CRUD REST APIs for Comment Resource"
)
public class CommentController {

    private CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "Create a new comment",
            description = "Create a new comment for a specific post",
            tags = {"Comment API"}
    )
    @ApiResponse(
            responseCode = "201",
            description = "The comment has been created successfully"
    )
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable(value = "postId") long postId, @RequestBody CommentDto commentDto){
        return new ResponseEntity<>(commentService.createComment(postId, commentDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all comments for a specific post",
            description = "Get all comments for a specific post",
            tags = {"Comment API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The comments have been retrieved successfully"
    )
    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> getCommentsByPostId(@PathVariable(value = "postId") long postId){
        return commentService.getByPostId(postId);
    }

    @Operation(
            summary = "Get a specific comment by ID",
            description = "Get a specific comment by ID",
            tags = {"Comment API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The comment has been retrieved successfully"
    )
    @GetMapping("posts/{postId}/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable long postId,@PathVariable long commentId){
        return commentService.getCommentById(postId, commentId);
    }

    @Operation(
            summary = "Update a specific comment by ID",
            description = "Update a specific comment by ID",
            tags = {"Comment API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The comment has been updated successfully"
    )
    @PutMapping("posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateCommentById(@PathVariable long postId, @PathVariable long commentId, @RequestBody CommentDto commentDto){
        return new ResponseEntity<>(commentService.updateCommentById(postId, commentId, commentDto), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a specific comment by ID",
            description = "Delete a specific comment by ID",
            tags = {"Comment API"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The comment has been deleted successfully"
    )
    @DeleteMapping("posts/{postId}/comments/{commentId}")
    public String deleteCommentById(@PathVariable long postId,@PathVariable long commentId){
        commentService.deleteCommentById(postId,commentId);
        return "Comment deleted successfully.";
    }
}
