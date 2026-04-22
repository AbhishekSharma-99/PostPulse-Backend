package com.postpulse.controller;

import com.postpulse.payload.PostDto;
import com.postpulse.payload.PostResponse;
import com.postpulse.payload.PostResponseDto;
import com.postpulse.service.PostService;
import com.postpulse.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Post API", description = "CRUD REST APIs for Post Resource")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create a post", description = "Create a new post. ADMIN only.")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostDto postDto) {
        return new ResponseEntity<>(postService.createPost(postDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all posts", description = "Retrieve all posts with pagination and sorting.")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    @GetMapping
    public ResponseEntity<PostResponse> getAllPost(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        return ResponseEntity.ok(postService.getAllPosts(pageNo, pageSize, sortBy, sortDir));
    }

    @Operation(summary = "Get post by ID", description = "Retrieve a specific post by its ID.")
    @ApiResponse(responseCode = "200", description = "Post retrieved successfully")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @Operation(summary = "Update post by ID", description = "Update a specific post by ID. ADMIN only.")
    @ApiResponse(responseCode = "200", description = "Post updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
            @Valid @RequestBody PostDto postDto,
            @PathVariable long id) {
        return ResponseEntity.ok(postService.updatePost(postDto, id));
    }

    @Operation(summary = "Delete post by ID", description = "Delete a specific post by ID. ADMIN only.")
    @ApiResponse(responseCode = "204", description = "Post deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get posts by category", description = "Retrieve all posts belonging to a specific category.")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId));
    }
}