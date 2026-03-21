package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.PostDto;
import com.postpulse.payload.PostResponse;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Tells JUnit 5 to enable Mockito annotations — without this, @Mock and @InjectMocks won't work
class PostServiceImplTest {

    // @Mock creates a fake version of these dependencies
    // so we control what they return without hitting a real database
    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    // @InjectMocks creates a real instance of PostServiceImpl
    // and automatically injects the @Mock objects above into it
    @InjectMocks
    private PostServiceImpl postService;

    // These are reusable test objects shared across test cases
    private PostDto postDto;
    private Post post;
    private Post savedPost;
    private Category category;

    @BeforeEach
    void setUp() {
        // @BeforeEach runs before every single test method
        // We build fresh objects here to avoid one test affecting another

        category = new Category();
        category.setId(1L);
        category.setName("Technology");

        postDto = new PostDto();
        postDto.setId(1L);
        postDto.setTitle("Test Title");
        postDto.setDescription("Test Description");
        postDto.setContent("Test Content");
        postDto.setCategoryId(1L);

        // 'post' is what ModelMapper produces from postDto (before saving)
        post = new Post();
        post.setTitle("Test Title");
        post.setDescription("Test Description");
        post.setContent("Test Content");

        // 'savedPost' is what the repository returns after saving (now has an ID)
        savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("Test Title");
        savedPost.setDescription("Test Description");
        savedPost.setContent("Test Content");
        savedPost.setCategory(category);
    }

    // =====================================================================
    // TEST CASES FOR createPost() METHOD
    // =====================================================================

    @Test
    @DisplayName("Should successfully create a post when category exists")
    void createPost_Success() {

        // --- ARRANGE (set up all the mocks) ---

        // WHY: categoryRepository is a mock (fake), it doesn't talk to a real DB.
        // So we tell it: "when someone calls findById(1L), pretend you found this category"
        // Without this, findById() would return empty Optional and throw ResourceNotFoundException
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // WHY: ModelMapper is mocked so it won't do real object mapping.
        // We tell it: "when map() is called with postDto, return our pre-built post object"
        // This isolates our test — we're testing service logic, not ModelMapper's behavior
        when(modelMapper.map(postDto, Post.class))
                .thenReturn(post);

        // WHY: postRepository is mocked so no real DB save happens.
        // We tell it: "when save() is called with any Post object, return our savedPost"
        // 'any(Post.class)' is used because the post object is modified (setCategory is called)
        // before saving, so we can't match on the exact object reference
        when(postRepository.save(any(Post.class)))
                .thenReturn(savedPost);

        // WHY: ModelMapper is called a second time to convert savedPost → PostDto.
        // We tell it: "when map() is called with savedPost, return our postDto"
        // We use eq() here to match the exact savedPost object returned by the repository
        PostDto expectedResponse = new PostDto();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Title");
        expectedResponse.setDescription("Test Description");
        expectedResponse.setContent("Test Content");
        expectedResponse.setCategoryId(1L);

        when(modelMapper.map(savedPost, PostDto.class))
                .thenReturn(expectedResponse);

        // --- ACT (call the actual method we're testing) ---
        PostDto result = postService.createPost(postDto);

        // --- ASSERT (verify the result is what we expect) ---

        // AssertJ fluent assertions — reads like English, much cleaner than JUnit assertEquals
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getContent()).isEqualTo("Test Content");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        // Verify that the mocks were actually called the expected number of times
        // This ensures our service isn't skipping any important steps
        verify(categoryRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(modelMapper, times(1)).map(postDto, Post.class);
        verify(modelMapper, times(1)).map(savedPost, PostDto.class);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void createPost_CategoryNotFound() {

        // --- ARRANGE ---

        // WHY: We simulate a scenario where the category ID doesn't exist in the DB.
        // Returning Optional.empty() forces the orElseThrow() in the service to trigger,
        // which should throw ResourceNotFoundException
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---

        // assertThatThrownBy is AssertJ's way of testing that an exception IS thrown.
        // It's cleaner than JUnit's @Test(expected=...) or try/catch blocks
        assertThatThrownBy(() -> postService.createPost(postDto))
                .isInstanceOf(ResourceNotFoundException.class);

        // Verify category lookup was attempted
        verify(categoryRepository, times(1)).findById(1L);

        // Verify that since category wasn't found, we never reached the save() call
        // This confirms our service exits early as expected
        verify(postRepository, never()).save(any(Post.class));
    }

    // =====================================================================
    // getAllPosts — Exact Matching
    // Reason: The service constructs a Pageable from 4 external parameters.
    // Exact matching ensures all 4 (pageNo, pageSize, sortBy, sortDir)
    // are correctly wired into the Pageable — any(Pageable.class) would
    // silently pass even if the service hardcoded wrong values.
    // =====================================================================

    @Test
    @DisplayName("Should return paginated posts with ASC sort direction")
    void getAllPosts_Success_AscendingSort() {

        // --- ARRANGE ---
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "title";
        String sortDir = "ASC";

        // Building the exact Pageable we expect the service to construct
        Pageable expectedPageable = PageRequest.of(
                pageNo, pageSize, Sort.by(sortBy).ascending()
        );

        Page<Post> postPage = new PageImpl<>(
                List.of(savedPost), expectedPageable, 1
        );

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        when(modelMapper.map(savedPost, PostDto.class))
                .thenReturn(postDto);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Title");
        assertThat(result.getPageNo()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElement()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();

        // Exact verify — wrong pageNo/pageSize/sort would fail here
        verify(postRepository, times(1)).findAll(expectedPageable);
        verify(modelMapper, times(1)).map(any(Post.class), eq(PostDto.class));
    }

    @Test
    @DisplayName("Should return paginated posts with DESC sort direction")
    void getAllPosts_Success_DescendingSort() {

        // --- ARRANGE ---
        int pageNo = 0;
        int pageSize = 5;
        String sortBy = "title";
        String sortDir = "DESC";

        Pageable expectedPageable = PageRequest.of(
                pageNo, pageSize, Sort.by(sortBy).descending()
        );

        Page<Post> postPage = new PageImpl<>(
                List.of(savedPost), expectedPageable, 1
        );

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        when(modelMapper.map(savedPost, PostDto.class))
                .thenReturn(postDto);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Title");

        verify(postRepository, times(1)).findAll(expectedPageable);
        verify(modelMapper, times(1)).map(any(Post.class), eq(PostDto.class));
    }

    @Test
    @DisplayName("Should return empty content when no posts exist")
    void getAllPosts_EmptyPage() {

        // --- ARRANGE ---
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "title";
        String sortDir = "ASC";

        Pageable expectedPageable = PageRequest.of(
                pageNo, pageSize, Sort.by(sortBy).ascending()
        );

        Page<Post> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(emptyPage);

        // --- ACT ---
        PostResponse result = postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElement()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();

        verify(postRepository, times(1)).findAll(expectedPageable);

        // ModelMapper completely untouched — no posts means no mapping
        verify(modelMapper, never()).map(any(Post.class), eq(PostDto.class));
    }

    // =====================================================================
    // getPostById — Exact Matching
    // Reason: findById() takes a simple Long ID — exact matching confirms
    // the service passed the correct ID without any ArgumentCaptor overhead.
    // ModelMapper uses any(Post.class) due to Mockito's technical limitation
    // with generic methods — not a strategy choice but a necessity.
    // =====================================================================

    @Test
    @DisplayName("Should return post when valid ID is provided")
    void getPostById_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        when(modelMapper.map(any(Post.class), eq(PostDto.class)))
                .thenReturn(postDto);

        // --- ACT ---
        PostDto result = postService.getPostById(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getContent()).isEqualTo("Test Content");

        // Exact verify — confirms service used the exact ID we passed
        verify(postRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(savedPost, PostDto.class);
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

        verify(postRepository, times(1)).findById(99L);
        verify(modelMapper, never()).map(any(Post.class), eq(PostDto.class));
    }

    // =====================================================================
    // updatePost — ArgumentCaptor
    // Reason: The service fetches an existing post then mutates MULTIPLE
    // fields (title, description, content, category) before saving.
    // ArgumentCaptor lets us intercept the exact mutated object and
    // assert every field was correctly updated — something any(Post.class)
    // completely misses and exact matching can't do since the object
    // is built and mutated entirely inside the service.
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

        PostDto updatedPostResponse = new PostDto();
        updatedPostResponse.setId(1L);
        updatedPostResponse.setTitle("Updated Title");
        updatedPostResponse.setDescription("Updated Description");
        updatedPostResponse.setContent("Updated Content");
        updatedPostResponse.setCategoryId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        // ArgumentCaptor — set the trap before the act
        // We'll use this to catch exactly what the service passes to save()
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        when(postRepository.save(any(Post.class)))
                .thenReturn(updatedPost);

        when(modelMapper.map(any(Post.class), eq(PostDto.class)))
                .thenReturn(updatedPostResponse);

        // --- ACT ---
        PostDto result = postService.updatePost(updatedPostDto, 1L);

        // --- ASSERT ---

        // First assert the returned DTO is correct
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getContent()).isEqualTo("Updated Content");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        // Now capture and inspect what was actually passed to save()
        // This is the ArgumentCaptor payoff — we see exactly what went in
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();

        // Assert every mutated field on the captured object
        // If the service forgot to call setDescription() for example, this fails
        assertThat(capturedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(capturedPost.getDescription()).isEqualTo("Updated Description");
        assertThat(capturedPost.getContent()).isEqualTo("Updated Content");
        assertThat(capturedPost.getCategory()).isEqualTo(category);
        assertThat(capturedPost.getCategory().getName()).isEqualTo("Technology");

        verify(categoryRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(any(Post.class), eq(PostDto.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during update")
    void updatePost_CategoryNotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(postDto, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, times(1)).findById(1L);
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during update")
    void updatePost_PostNotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(postDto, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).findById(99L);
        verify(postRepository, never()).save(any(Post.class));
    }
}