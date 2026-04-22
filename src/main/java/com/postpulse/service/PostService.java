package com.postpulse.service;

import com.postpulse.payload.PostDto;
import com.postpulse.payload.PostResponse;
import com.postpulse.payload.PostResponseDto;

import java.util.List;

public interface PostService {
    PostResponseDto createPost(PostDto postDto);

    PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String dir);

    PostResponseDto getPostById(long id);

    PostResponseDto updatePost(PostDto postDto, long id);

    void deletePost(long id);

    List<PostResponseDto> getPostsByCategory(long categoryId);


}
