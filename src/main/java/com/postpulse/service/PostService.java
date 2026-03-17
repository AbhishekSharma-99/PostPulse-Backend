package com.postpulse.service;

import com.postpulse.payload.PostDto;
import com.postpulse.payload.PostResponse;

import java.util.List;

public interface PostService {
    PostDto createPost(PostDto postDto);

    PostResponse getAllPosts(int pageno, int pagesize, String sortBy, String dir);

    PostDto getPostById(long id);

    PostDto updatePost(PostDto postDto, long id);

    void deletePost(long id);

    List<PostDto> getPostsByCategory(long categoryId);


}
