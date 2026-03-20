package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.PostDto;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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
}