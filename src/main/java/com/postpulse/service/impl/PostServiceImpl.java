package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.post.*;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.repository.PostRepository;
import com.postpulse.service.PostService;
import com.postpulse.utils.SecurityUtils;
import com.postpulse.utils.SlugUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ModelMapper mapper;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;
    private final SlugUtils slugUtils;

    public PostServiceImpl(PostRepository postRepository,
                           ModelMapper mapper,
                           CategoryRepository categoryRepository,
                           SecurityUtils securityUtils,
                           SlugUtils slugUtils) {
        this.postRepository = postRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.securityUtils = securityUtils;
        this.slugUtils = slugUtils;
    }

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest postCreateRequest) {
        Category category = categoryRepository.findById(postCreateRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postCreateRequest.getCategoryId()));

        User user = securityUtils.getCurrentUser();

        Post post = mapToEntity(postCreateRequest);
        post.setCategory(category);
        post.setUser(user);
        String uniqueSlug = slugUtils.generateUniqueSlug(post.getTitle(), null);
        post.setSlug(uniqueSlug);

        Post savedPost = postRepository.save(post);
        log.info("Created post with id: {}, slug: {}", savedPost.getId(), savedPost.getSlug());
        return mapToDto(savedPost);
    }

    @Override
    public PostPageResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> posts = postRepository.findAll(pageable);

        List<PostSummary> content = posts.getContent()
                .stream()
                .map(this::mapToSummaryDto)
                .toList();

        return new PostPageResponse(
                content,
                posts.getNumber(),
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.isLast()
        );
    }

    @Override
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return mapToDto(post);
    }

    @Override
    @Transactional
    public PostResponse updatePost(PostUpdateRequest postUpdateRequest, Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        Category category = categoryRepository.findById(postUpdateRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postUpdateRequest.getCategoryId()));

        boolean titleChanged = !post.getTitle().equals(postUpdateRequest.getTitle());
        if (titleChanged) {
            String newSlug = slugUtils.generateUniqueSlug(postUpdateRequest.getTitle(), id);
            post.setSlug(newSlug);
        }

        post.setTitle(postUpdateRequest.getTitle());
        post.setDescription(postUpdateRequest.getDescription());
        post.setContent(postUpdateRequest.getContent());
        post.setCategory(category);

        Post updatedPost = postRepository.save(post);
        log.info("Updated post with id: {}", updatedPost.getId());
        return mapToDto(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        int deletedRows = postRepository.deletePostById(id);
        if (deletedRows == 0) {
            throw new ResourceNotFoundException("Post", "id", id);
        }

        log.info("Deleted post with id: {}", id);
    }

    @Override
    public List<PostSummary> getPostsByCategory(Long categoryId) {
        return postRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToSummaryDto)
                .toList();
    }

    @Override
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "slug", slug));
        return mapToDto(post);
    }

    private Post mapToEntity(PostCreateRequest postCreateRequest) {
        return mapper.map(postCreateRequest, Post.class);
    }

    private PostResponse mapToDto(Post post) {
        return mapper.map(post, PostResponse.class);
    }

    private PostSummary mapToSummaryDto(Post post) {
        return mapper.map(post, PostSummary.class);
    }
}