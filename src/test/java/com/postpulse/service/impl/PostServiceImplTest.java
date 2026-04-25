package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.PostDto;
import com.postpulse.payload.PostResponse;
import com.postpulse.payload.PostResponseDto;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    // @Spy wraps a real ModelMapper instance — actual mapping logic runs,
    // including the custom PropertyMap that populates categoryId from
    // post.getCategory().getId(). A @Mock here would prove map() was called
    // but never that the field mapping itself is correct.
    @Spy
    private ModelMapper modelMapper = buildModelMapper();

    @InjectMocks
    private PostServiceImpl postService;

    private PostDto postDto;
    private Post savedPost;
    private Category category;

    // Mirrors the production @Bean exactly — same PropertyMap, same registration.
    // Any divergence here would mean tests pass while production silently
    // maps categoryId as 0.
    private static ModelMapper buildModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(new PropertyMap<Post, PostResponseDto>() {
            @Override
            protected void configure() {
                map().setCategoryId(source.getCategory().getId());
            }
        });
        return modelMapper;
    }

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Technology");

        // postDto is the incoming request object — no ID (client doesn't set it)
        postDto = new PostDto();
        postDto.setTitle("Test Title");
        postDto.setDescription("Test Description");
        postDto.setContent("Test Content");
        postDto.setCategoryId(1L);

        // 'savedPost' is what the repository returns after save() —
        // category must be set here so the real ModelMapper can traverse
        // getCategory().getId() without a NullPointerException
        savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("Test Title");
        savedPost.setDescription("Test Description");
        savedPost.setContent("Test Content");
        savedPost.setCategory(category);
    }

    // =====================================================================
    // createPost — ArgumentCaptor on save()
    // postRepository.save() uses ArgumentCaptor because the post object
    // is mutated (setCategory) inside the service before persisting —
    // we capture the mutated state and assert the category was correctly
    // assigned, which any(Post.class) would silently let through.
    // With real ModelMapper, output fields are asserted directly —
    // no map() stubs needed, proving transformation works end-to-end.
    // =====================================================================

    @Test
    @DisplayName("Should successfully create a post when category exists")
    void createPost_Success() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.save(any(Post.class)))
                .thenReturn(savedPost);

        // --- ACT ---
        PostResponseDto result = postService.createPost(postDto);

        // --- ASSERT ---

        // Real ModelMapper ran — these values came from actual field mapping,
        // not a stub. If the PropertyMap breaks, this assertion catches it.
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getContent()).isEqualTo("Test Content");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(category);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void createPost_CategoryNotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.createPost(postDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    // =====================================================================
    // getAllPosts — Exact Pageable Matching
    // The service constructs a Pageable from 4 external parameters
    // (pageNo, pageSize, sortBy, sortDir). Exact matching ensures all 4 are
    // correctly wired into PageRequest — using any(Pageable.class) would
    // silently pass even if the service hardcoded wrong values or ignored
    // the sort direction entirely.
    // =====================================================================

    @Test
    @DisplayName("Should return paginated posts with ASC sort direction")
    void getAllPosts_Success_AscendingSort() {

        // --- ARRANGE ---
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<Post> postPage = new PageImpl<>(List.of(savedPost), expectedPageable, 1);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(0, 10, "title", "ASC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Title");
        assertThat(result.getContent().getFirst().getCategoryId()).isEqualTo(1L);
        assertThat(result.getPageNo()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElement()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should return paginated posts with DESC sort direction")
    void getAllPosts_Success_DescendingSort() {

        // --- ARRANGE ---

        // Descending sort — verifies the ternary branch in the service
        // that switches between ascending() and descending() based on sortDir
        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("title").descending());
        Page<Post> postPage = new PageImpl<>(List.of(savedPost), expectedPageable, 1);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(0, 5, "title", "DESC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Title");

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should return empty content when no posts exist")
    void getAllPosts_EmptyPage() {

        // --- ARRANGE ---
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        // Empty page simulates a database with no posts —
        // verifies that the service handles empty results and correctly identifies
        // the page as the last one to prevent pagination logic errors.
        Page<Post> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(emptyPage);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(0, 10, "title", "ASC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElement()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should default to descending sort when sort direction is invalid")
    void getAllPosts_InvalidSortDirection_DefaultsToDescending() {

        // --- ARRANGE ---

        // The service ternary checks equalsIgnoreCase("ASC") — anything that
        // isn't "ASC" silently falls through to descending(). This test
        // documents that contract explicitly so a future refactor can't
        // accidentally change the default without breaking a test.
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("title").descending());
        Page<Post> postPage = new PageImpl<>(List.of(savedPost), expectedPageable, 1);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(0, 10, "title", "INVALID");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(postRepository).findAll(expectedPageable);
    }

    // =====================================================================
    // getPostById — Exact Matching
    // findById() takes a simple Long ID — exact matching confirms the
    // service passed the correct ID. The not-found case verifies
    // orElseThrow() fires before any mapping is attempted.
    // =====================================================================

    @Test
    @DisplayName("Should return post when valid ID is provided")
    void getPostById_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        // --- ACT ---
        PostResponseDto result = postService.getPostById(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getContent()).isEqualTo("Test Content");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post ID does not exist")
    void getPostById_NotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.getPostById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);
    }

    // =====================================================================
    // updatePost — ArgumentCaptor
    // The service fetches an existing post then mutates MULTIPLE fields
    // (title, description, content, category) before calling save().
    // ArgumentCaptor intercepts the exact mutated Post object and lets us
    // assert every field was correctly set — any(Post.class) would pass
    // even if the service forgot to call setDescription(), and exact
    // matching is impossible since the object is built and mutated entirely
    // inside the service method itself.
    // =====================================================================

    @Test
    @DisplayName("Should successfully update post when post and category both exist")
    void updatePost_Success() {

        // --- ARRANGE ---
        PostDto updatedPostDto = new PostDto();
        updatedPostDto.setTitle("Updated Title");
        updatedPostDto.setDescription("Updated Description");
        updatedPostDto.setContent("Updated Content");
        updatedPostDto.setCategoryId(1L);

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Updated Title");
        updatedPost.setDescription("Updated Description");
        updatedPost.setContent("Updated Content");
        updatedPost.setCategory(category);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        when(postRepository.save(any(Post.class)))
                .thenReturn(updatedPost);

        // --- ACT ---
        PostResponseDto result = postService.updatePost(updatedPostDto, 1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getContent()).isEqualTo("Updated Content");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        // Capture what was actually passed to save() and inspect every field —
        // if the service forgot to call setDescription() or setCategory(),
        // these assertions catch it
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(capturedPost.getDescription()).isEqualTo("Updated Description");
        assertThat(capturedPost.getContent()).isEqualTo("Updated Content");
        assertThat(capturedPost.getCategory()).isEqualTo(category);

        verify(categoryRepository).findById(1L);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during update")
    void updatePost_CategoryNotFound() {

        // --- ARRANGE ---

        // Category not found — service should throw before even fetching the post
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(postDto, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(1L);

        // Post fetch and save must never happen — category check comes first
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during update")
    void updatePost_PostNotFound() {

        // --- ARRANGE ---

        // Category exists but post doesn't — service should throw after
        // the category check passes but before save() is called
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(postDto, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(postRepository).findById(99L);
        verify(postRepository, never()).save(any(Post.class));
    }

    // =====================================================================
    // deletePost — Exact Matching
    // No mutation happens — service just fetches and deletes.
    // Exact object match on delete() confirms the right post was removed,
    // not just any Post that happened to pass through.
    // =====================================================================

    @Test
    @DisplayName("Should successfully delete post when valid ID is provided")
    void deletePost_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        // --- ACT ---
        postService.deletePost(1L);

        // --- ASSERT ---
        verify(postRepository).findById(1L);
        verify(postRepository).delete(savedPost);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post ID does not exist during delete")
    void deletePost_NotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);

        // Most critical assertion for delete — nothing must be deleted
        // when the post doesn't exist
        verify(postRepository, never()).delete(any(Post.class));
    }

    // =====================================================================
    // getPostsByCategory — Exact Matching
    // findByCategoryId() takes a simple Long ID — exact matching confirms
    // the service passed the correct categoryId. The empty list scenario
    // verifies the method returns silently rather than throwing — a category
    // with no posts is valid state, not an error.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped list of posts when valid category ID is provided")
    void getPostsByCategory_Success() {

        // --- ARRANGE ---
        when(postRepository.findByCategoryId(1L))
                .thenReturn(List.of(savedPost));

        // --- ACT ---
        List<PostResponseDto> result = postService.getPostsByCategory(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test Title");
        assertThat(result.getFirst().getDescription()).isEqualTo("Test Description");
        assertThat(result.getFirst().getContent()).isEqualTo("Test Content");
        assertThat(result.getFirst().getCategoryId()).isEqualTo(1L);

        verify(postRepository).findByCategoryId(1L);
    }

    @Test
    @DisplayName("Should return empty list when no posts exist for given category ID")
    void getPostsByCategory_EmptyList() {

        // --- ARRANGE ---

        // Empty list simulates either a valid categoryId with no associated posts
        // OR an invalid categoryId entirely — this method treats both identically
        // by design, returning an empty list rather than throwing an exception
        when(postRepository.findByCategoryId(99L))
                .thenReturn(List.of());

        // --- ACT ---
        List<PostResponseDto> result = postService.getPostsByCategory(99L);

        // --- ASSERT ---
        assertThat(result).isNotNull().isEmpty();

        verify(postRepository).findByCategoryId(99L);
    }
}