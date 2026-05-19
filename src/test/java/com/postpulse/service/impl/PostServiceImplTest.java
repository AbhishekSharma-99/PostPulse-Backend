package com.postpulse.service.impl;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.post.*;
import com.postpulse.repository.CategoryRepository;
import com.postpulse.repository.PostRepository;
import com.postpulse.utils.SecurityUtils;
import com.postpulse.utils.SlugUtils;
import com.postpulse.utils.TestModelMapper;
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

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private SlugUtils slugUtils;

    // @Spy wraps a real ModelMapper instance — actual mapping logic runs,
    // including both custom PropertyMaps that populate categoryId, categoryName,
    // and authorName from nested associations. A @Mock here would prove map()
    // was called but never that the field mapping itself is correct.
    @Spy
    private ModelMapper modelMapper = TestModelMapper.getModelMapper();

    @InjectMocks
    private PostServiceImpl postService;

    private Category category;
    private User user;
    private Post savedPost;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Technology");

        user = new User();
        user.setId(10L);
        user.setName("Abhishek Sharma");

        // savedPost is what the repository returns after save() —
        // both category and user must be set so the real ModelMapper can
        // traverse getCategory().getId(), getCategory().getName(), and
        // getUser().getName() without a NullPointerException.
        savedPost = new Post();
        savedPost.setId(1L);
        savedPost.setTitle("Introduction to Spring Boot");
        savedPost.setDescription("A deep dive into Spring Boot internals");
        savedPost.setContent("Spring Boot auto-configuration works by scanning the classpath...");
        savedPost.setSlug("introduction-to-spring-boot");
        savedPost.setCategory(category);
        savedPost.setUser(user);

        // createRequest is the incoming DTO — no ID, no slug, no user.
        // Author is resolved from the SecurityContext inside the service.
        createRequest = new PostCreateRequest();
        createRequest.setTitle("Introduction to Spring Boot");
        createRequest.setDescription("A deep dive into Spring Boot internals");
        createRequest.setContent("Spring Boot auto-configuration works by scanning the classpath...");
        createRequest.setCategoryId(1L);

        updateRequest = new PostUpdateRequest();
        updateRequest.setTitle("Introduction to Spring Boot — Revised");
        updateRequest.setDescription("An updated deep dive into Spring Boot internals");
        updateRequest.setContent("Spring Boot 3 requires Java 17 as a baseline...");
        updateRequest.setCategoryId(1L);
    }



    // =====================================================================
    // createPost — ArgumentCaptor on save()
    // The service fetches category, resolves the authenticated user from
    // SecurityContext, maps the DTO to an entity, then mutates the entity
    // (setCategory, setUser, setSlug) before calling save(). ArgumentCaptor
    // intercepts the fully mutated Post and asserts every field was set —
    // any(Post.class) would silently pass even if setUser() was never called.
    // Real ModelMapper runs end-to-end — if the PropertyMap breaks, the
    // authorName / categoryName assertions catch it immediately.
    // =====================================================================

    @Test
    @DisplayName("Should successfully create a post when category and user both exist")
    void createPost_Success() {

        // --- ARRANGE ---

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(securityUtils.getCurrentUser()).thenReturn(user);

        when(slugUtils.generateUniqueSlug("Introduction to Spring Boot", null))
                .thenReturn("introduction-to-spring-boot");

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // --- ACT ---
        PostResponse result = postService.createPost(createRequest);

        // --- ASSERT ---

        // Real ModelMapper ran — these values came from the PropertyMap traversal,
        // not stubs. If getCategory() or getUser() is null on the returned entity,
        // categoryName and authorName would be null here and fail immediately.
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Introduction to Spring Boot");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Technology");
        assertThat(result.getAuthorName()).isEqualTo("Abhishek Sharma");

        // Capture the mutated Post passed to save() — verifies that category,
        // user, and slug were all assigned before persistence, not after.
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post captured = captor.getValue();
        assertThat(captured.getCategory()).isEqualTo(category);
        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getSlug()).isEqualTo("introduction-to-spring-boot");

        verify(categoryRepository).findById(1L);
        verify(securityUtils).getCurrentUser();
        verify(slugUtils).generateUniqueSlug("Introduction to Spring Boot", null);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist on create")
    void createPost_CategoryNotFound() {

        // --- ARRANGE ---

        // No SecurityContext setup needed — the service fetches category first.
        // If that throws, resolveAuthenticatedUserId() is never reached.
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.createPost(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when authenticated user does not exist in DB")
    void createPost_UserNotFound() {

        // --- ARRANGE ---
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // SecurityUtils encapsulates the DB lookup — it throws when the user
        // referenced in the JWT no longer exists. The service never sees UserRepository.
        when(securityUtils.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("User", "id", 10L));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.createPost(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(1L);
        verify(securityUtils).getCurrentUser();
        verify(postRepository, never()).save(any(Post.class));
    }

    // =====================================================================
    // getAllPosts — Exact Pageable Matching
    // The service constructs a Pageable from 4 external parameters
    // (pageNo, pageSize, sortBy, sortDir). Exact matching ensures all 4 are
    // correctly wired into PageRequest — using any(Pageable.class) would
    // silently pass even if the service hardcoded wrong values or ignored
    // the sort direction entirely. PostSummary is asserted on content
    // because getAllPosts streams through mapToSummary, not mapToDto.
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
        PostPageResponse result = postService.getAllPosts(0, 10, "title", "ASC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Introduction to Spring Boot");
        assertThat(result.getContent().getFirst().getCategoryName()).isEqualTo("Technology");
        assertThat(result.getContent().getFirst().getAuthorName()).isEqualTo("Abhishek Sharma");
        assertThat(result.getPageNo()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should return paginated posts with DESC sort direction")
    void getAllPosts_Success_DescendingSort() {

        // --- ARRANGE ---

        // Descending branch — verifies the ternary in the service correctly
        // switches between ascending() and descending() based on sortDir.
        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("title").descending());
        Page<Post> postPage = new PageImpl<>(List.of(savedPost), expectedPageable, 1);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        // --- ACT ---
        PostPageResponse result = postService.getAllPosts(0, 5, "title", "DESC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Introduction to Spring Boot");

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should return empty content list when no posts exist")
    void getAllPosts_EmptyPage() {

        // --- ARRANGE ---
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        // Empty page simulates a database with no posts —
        // verifies the service handles zero-result pages gracefully and correctly
        // flags isLast() as true to prevent the client from requesting page 1.
        Page<Post> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(emptyPage);

        // --- ACT ---
        PostPageResponse result = postService.getAllPosts(0, 10, "title", "ASC");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();

        verify(postRepository).findAll(expectedPageable);
    }

    @Test
    @DisplayName("Should default to descending sort when sort direction is not ASC")
    void getAllPosts_InvalidSortDirection_DefaultsToDescending() {

        // --- ARRANGE ---

        // The service ternary checks equalsIgnoreCase("ASC") — anything else falls
        // through to descending(). This test pins that contract explicitly so a
        // future refactor can't silently change the default without a test failure.
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by("title").descending());
        Page<Post> postPage = new PageImpl<>(List.of(savedPost), expectedPageable, 1);

        when(postRepository.findAll(expectedPageable))
                .thenReturn(postPage);

        // --- ACT ---
        PostPageResponse result = postService.getAllPosts(0, 10, "title", "INVALID");

        // --- ASSERT ---
        assertThat(result.getContent()).hasSize(1);

        verify(postRepository).findAll(expectedPageable);
    }

    // =====================================================================
    // getPostById — Exact Matching
    // findById() takes a simple Long — exact matching confirms the correct
    // ID was passed. The not-found case verifies orElseThrow() fires before
    // any mapping is attempted, so mapToDto() is never called on a null Post.
    // =====================================================================

    @Test
    @DisplayName("Should return full post detail when valid ID is provided")
    void getPostById_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        // --- ACT ---
        PostResponse result = postService.getPostById(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Introduction to Spring Boot");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Technology");
        assertThat(result.getAuthorName()).isEqualTo("Abhishek Sharma");

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
    // updatePost — ArgumentCaptor on save()
    // The service fetches the post, fetches category, conditionally generates
    // a new slug only when the title changed, then mutates multiple fields
    // before save(). Two distinct paths exist: title-changed and title-unchanged.
    // ArgumentCaptor asserts every mutated field — any(Post.class) would pass
    // even if setDescription() or setCategory() was silently dropped.
    // The slug assertion specifically verifies the conditional branch fires
    // correctly in each path.
    // =====================================================================

    @Test
    @DisplayName("Should update all fields and regenerate slug when title has changed")
    void updatePost_Success_TitleChanged() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        // Title changed — service must call generateUniqueSlug with the new title
        // and the existing post ID (excludePostId) so the slug uniqueness check
        // doesn't consider the current post's own slug as a conflict.
        when(slugUtils.generateUniqueSlug("Introduction to Spring Boot — Revised", 1L))
                .thenReturn("introduction-to-spring-boot-revised");

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Introduction to Spring Boot — Revised");
        updatedPost.setDescription("An updated deep dive into Spring Boot internals");
        updatedPost.setContent("Spring Boot 3 requires Java 17 as a baseline...");
        updatedPost.setSlug("introduction-to-spring-boot-revised");
        updatedPost.setCategory(category);
        updatedPost.setUser(user);

        when(postRepository.save(any(Post.class)))
                .thenReturn(updatedPost);

        // --- ACT ---
        PostResponse result = postService.updatePost(updateRequest, 1L);

        // --- ASSERT ---
        assertThat(result.getTitle()).isEqualTo("Introduction to Spring Boot — Revised");
        assertThat(result.getCategoryName()).isEqualTo("Technology");
        assertThat(result.getAuthorName()).isEqualTo("Abhishek Sharma");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("Introduction to Spring Boot — Revised");
        assertThat(captured.getDescription()).isEqualTo("An updated deep dive into Spring Boot internals");
        assertThat(captured.getContent()).isEqualTo("Spring Boot 3 requires Java 17 as a baseline...");
        assertThat(captured.getSlug()).isEqualTo("introduction-to-spring-boot-revised");
        assertThat(captured.getCategory()).isEqualTo(category);

        verify(slugUtils).generateUniqueSlug("Introduction to Spring Boot — Revised", 1L);
    }

    @Test
    @DisplayName("Should update fields but not regenerate slug when title is unchanged")
    void updatePost_Success_TitleUnchanged() {

        // --- ARRANGE ---

        // Request title matches the existing post title exactly —
        // the service must skip slug regeneration and leave the existing
        // slug untouched. slugUtils must never be invoked.
        PostUpdateRequest sameTitle = new PostUpdateRequest();
        sameTitle.setTitle("Introduction to Spring Boot");     // same as savedPost.getTitle()
        sameTitle.setDescription("Revised description only");
        sameTitle.setContent("Spring Boot auto-configuration works by scanning the classpath...");
        sameTitle.setCategoryId(1L);

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(postRepository.save(any(Post.class)))
                .thenReturn(savedPost);

        // --- ACT ---
        postService.updatePost(sameTitle, 1L);

        // --- ASSERT ---

        // Most important assertion in this test — slug generation must be
        // skipped entirely when the title hasn't changed.
        verify(slugUtils, never()).generateUniqueSlug(anyString(), any());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("introduction-to-spring-boot");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during update")
    void updatePost_PostNotFound() {

        // --- ARRANGE ---

        // Post not found — service throws before the category lookup.
        // The execution order in updatePost is: findById(post) first,
        // then findById(category). This is the opposite of the old impl.
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(updateRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);
        verify(categoryRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist during update")
    void updatePost_CategoryNotFound() {

        // --- ARRANGE ---

        // Post exists but category doesn't — service throws after the post fetch
        // but before save(). Verifies orElseThrow() on the category lookup fires.
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(savedPost));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.updatePost(updateRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    // =====================================================================
    // deletePost — Exact Matching on deletePostById()
    // The new impl uses a @Modifying JPQL query returning int instead of
    // findById() + delete(entity). Return value of 0 means no row was deleted
    // — service must throw ResourceNotFoundException in that case.
    // No ArgumentCaptor needed — the only input is the Long ID.
    // =====================================================================

    @Test
    @DisplayName("Should successfully delete post when valid ID is provided")
    void deletePost_Success() {

        // --- ARRANGE ---

        // deletePostById returns 1 — exactly one row was deleted.
        // Service must return without throwing.
        when(postRepository.deletePostById(1L))
                .thenReturn(1);

        // --- ACT ---
        postService.deletePost(1L);

        // --- ASSERT ---
        verify(postRepository).deletePostById(1L);

        // Confirms the old findById + delete(entity) path is gone —
        // no SELECT should precede the DELETE in this implementation.
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post ID does not exist during delete")
    void deletePost_NotFound() {

        // --- ARRANGE ---

        // deletePostById returns 0 — no row matched the ID.
        // Service must detect this and throw rather than silently succeeding.
        when(postRepository.deletePostById(99L))
                .thenReturn(0);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).deletePostById(99L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    // =====================================================================
    // getPostsByCategory — Exact Matching
    // Returns List<PostSummary> not List<PostResponse> — the mapping
    // goes through mapToSummary which uses the Post→PostSummary
    // PropertyMap. Asserting categoryName and authorName on the summary
    // confirms the correct PropertyMap ran, not the response one.
    // Empty list is valid state — a category with no posts should never throw.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped PostSummary list when valid category ID is provided")
    void getPostsByCategory_Success() {

        // --- ARRANGE ---
        when(postRepository.findByCategoryId(1L))
                .thenReturn(List.of(savedPost));

        // --- ACT ---
        List<PostSummary> result = postService.getPostsByCategory(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getTitle()).isEqualTo("Introduction to Spring Boot");
        assertThat(result.getFirst().getSlug()).isEqualTo("introduction-to-spring-boot");
        assertThat(result.getFirst().getCategoryName()).isEqualTo("Technology");
        assertThat(result.getFirst().getAuthorName()).isEqualTo("Abhishek Sharma");

        verify(postRepository).findByCategoryId(1L);
    }

    @Test
    @DisplayName("Should return empty list when no posts exist for the given category ID")
    void getPostsByCategory_EmptyList() {

        // --- ARRANGE ---

        // Empty list simulates a valid category with no posts yet —
        // treated identically to an unknown categoryId by design.
        // Service must return an empty list, not throw.
        when(postRepository.findByCategoryId(99L))
                .thenReturn(List.of());

        // --- ACT ---
        List<PostSummary> result = postService.getPostsByCategory(99L);

        // --- ASSERT ---
        assertThat(result).isNotNull().isEmpty();

        verify(postRepository).findByCategoryId(99L);
    }

    // =====================================================================
    // getPostBySlug — Exact Matching
    // findBySlug() takes a String slug — exact matching confirms the correct
    // slug was passed through. Not-found verifies orElseThrow() fires before
    // any mapping is attempted.
    // =====================================================================

    @Test
    @DisplayName("Should return full post detail when valid slug is provided")
    void getPostBySlug_Success() {

        // --- ARRANGE ---
        when(postRepository.findBySlug("introduction-to-spring-boot"))
                .thenReturn(Optional.of(savedPost));

        // --- ACT ---
        PostResponse result = postService.getPostBySlug("introduction-to-spring-boot");

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Introduction to Spring Boot");
        assertThat(result.getSlug()).isEqualTo("introduction-to-spring-boot");
        assertThat(result.getCategoryName()).isEqualTo("Technology");
        assertThat(result.getAuthorName()).isEqualTo("Abhishek Sharma");

        verify(postRepository).findBySlug("introduction-to-spring-boot");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when slug does not exist")
    void getPostBySlug_NotFound() {

        // --- ARRANGE ---
        when(postRepository.findBySlug("non-existent-slug"))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> postService.getPostBySlug("non-existent-slug"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findBySlug("non-existent-slug");
    }
}