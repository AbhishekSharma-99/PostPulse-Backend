package com.springboot.blog.controller;

import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.service.PostService;
import com.springboot.blog.utils.AppConstants;
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

@Tag(
        name = "Post API",
        description = "CRUD REST APIs for Post Resource"
)
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @Operation(
            summary = "Create post REST API",
            description = "Create post REST API is used to save a post in database and"+
                    "Only authenticated ADMIN users can CREATE a post"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Post created successfully"
    )
    @SecurityRequirement(
            name = "bearerAuth"
    )
    //create blog post rest api
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/posts")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto) {
        return new ResponseEntity<>(postService.createPost(postDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all posts REST API",
            description = "Get all posts REST API is used to retrieve all posts from database"+
                    "Only authenticated ADMIN users can GET all posts"
    )
    @ApiResponse(
            responseCode = "200",
            description = "All posts retrieved successfully"
    )
    //get all post rest api
    @GetMapping("/api/v1/posts")
    public PostResponse getAllPost(
            @RequestParam(value = "pageno",defaultValue = AppConstants.DEAFAULT_PAGE_NUMBER, required = false) int pageno,
            @RequestParam(value = "pagesize",defaultValue = AppConstants.DEAFAULT_PAGE_SIZE, required = false) int pagesize,
            @RequestParam(value = "sortby",defaultValue = AppConstants.DEAFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir",defaultValue = AppConstants.DEAFAULT_SORT_DIRECTION, required = false) String sortDir
    ) {
        return postService.getAllPosts(pageno,pagesize,sortBy,sortDir);
    }

    @Operation(
            summary = "Get Post By Id  REST API",
            description = "Get Post By Id REST API is used to retrieve a specific post from the database"+
                    " using the given Id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Post retrieved successfully"
    )
    //get post by id rest api
    @GetMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable(name = "id") long id){
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @Operation(
            summary = "Update Post By Id  REST API",
            description = "Update Post By Id REST API is used to update a specific post in the database"+
                    " using the given Id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Post updated successfully"
    )
    @SecurityRequirement(
            name = "bearerAuth"
    )
    //update post by id rest api
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> updatePost(@Valid @RequestBody PostDto postDto, @PathVariable(name = "id") long id){
        return new ResponseEntity<>(postService.updatePost(postDto, id), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete Post By Id  REST API",
            description = "Delete Post By Id REST API is used to delete a specific post from the database"+
                    " using the given Id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Post deleted successfully"
    )
    @SecurityRequirement(
            name = "bearerAuth"
    )
    //delete post by id rest api
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/v1/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable(name = "id") long id){
        postService.deletePost(id);
        return new ResponseEntity<>("The post entity deleted successfully", HttpStatus.OK);
    }

    @Operation(
            summary = "Get Post By Category  REST API",
            description = "Get Post By Category REST API is used to retrieve all posts belonging to a specific category"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully"
    )
    //get post by category rest api
    @GetMapping("/api/v1/posts/category/{categoryId}")
    public ResponseEntity<List<PostDto>> getPostsByCategory(@PathVariable long categoryId){
        List<PostDto> response = postService.getPostsByCategory(categoryId);
        return ResponseEntity.ok(response);
    }

}
