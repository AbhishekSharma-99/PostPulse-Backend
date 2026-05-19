package com.postpulse.service;

import com.postpulse.payload.post.*;

import java.util.List;

public interface PostService {
    PostResponse createPost(PostCreateRequest postCreateRequest);

    PostPageResponse getAllPosts(int pageNo, int pageSize, String sortBy, String dir);

    PostResponse getPostById(Long id);

    PostResponse updatePost(PostUpdateRequest postUpdateRequest, Long id);

    void deletePost(Long id);

    List<PostSummary> getPostsByCategory(Long categoryId);

    PostResponse getPostBySlug(String slug);
}
