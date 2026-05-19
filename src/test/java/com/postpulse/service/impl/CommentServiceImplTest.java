package com.postpulse.service.impl;

import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.comment.CommentRequest;
import com.postpulse.payload.comment.CommentResponse;
import com.postpulse.repository.CommentRepository;
import com.postpulse.repository.PostRepository;
import com.postpulse.utils.SecurityUtils;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private SecurityUtils securityUtils;

    // @Spy wraps a real ModelMapper instance — actual mapping logic executes.
    // Comment has no custom PropertyMap, so default field-name matching covers
    // all fields. A @Mock would only prove map() was invoked, never that
    // id/body were actually populated correctly.
    @Spy
    private ModelMapper modelMapper = TestModelMapper.getModelMapper();

    @InjectMocks
    private CommentServiceImpl commentService;

    private Post post;
    private User user;
    private Comment savedComment;
    private CommentRequest commentRequest;

    private static Comment buildForeignComment(User user) {
        Post otherPost = new Post();
        otherPost.setId(2L); // differs from post.id = 1 used in all requests

        Comment foreignComment = new Comment();
        foreignComment.setId(1L);
        foreignComment.setBody("This is a well-written and detailed post.");
        foreignComment.setPost(otherPost); // parented to post 2 — request targets post 1
        foreignComment.setUser(user);      // user must be set so getId() doesn't NPE
        return foreignComment;
    }

    // =====================================================================
    // Static fixture factories
    //
    // buildForeignComment() — produces a Comment whose parent post id differs
    // from the post id used in all requests. This mismatch is what triggers
    // validateCommentBelongsToPost() to throw BlogAPIException.
    // Used by: getCommentById, updateCommentById, deleteCommentById
    // ownership-violation branches.
    //
    // buildAdminAuthorities() / buildUserAuthorities() — return authority sets
    // used to stub securityUtils.getCurrentUserAuthorities() in tests that
    // reach validateCommentBelongsToUser(). Extracted here to keep test
    // bodies readable.
    // =====================================================================

    private static Collection<GrantedAuthority> adminAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static Collection<GrantedAuthority> userAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1L);
        post.setTitle("Spring Boot Internals");
        post.setDescription("Deep dive into Spring Boot auto-configuration");
        post.setContent("Full content here...");

        user = new User();
        user.setId(10L);
        user.setName("Abhishek Sharma");
        user.setUsername("abhishek");
        user.setEmail("abhishek@postpulse.com");
        user.setPassword("hashed_password");

        // CommentRequest — incoming payload; no id field by design
        commentRequest = new CommentRequest();
        commentRequest.setBody("This is a well-written and detailed post.");

        // savedComment — what the repository returns after save().
        // post and user must be set so mapToDto() can traverse
        // comment.getPost().getId() and comment.getUser().getUsername()
        // without NullPointerException.
        savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setBody("This is a well-written and detailed post.");
        savedComment.setPost(post);
        savedComment.setUser(user);
    }

    // =====================================================================
    // createComment
    //
    // The service fetches post → fetches current user → maps request to entity
    // → mutates entity (setPost, setUser) → saves → maps to response.
    // ArgumentCaptor confirms both mutations happened before persist.
    // Real ModelMapper runs via @Spy to verify actual field mapping.
    // =====================================================================

    @Test
    @DisplayName("Should create and return CommentResponse when post exists and user is authenticated")
    void createComment_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        // getCurrentUser() fires a DB query inside SecurityUtils — mocked here
        // so no real SecurityContext or UserRepository is needed in unit scope
        when(securityUtils.getCurrentUser())
                .thenReturn(user);

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        // --- ACT ---
        CommentResponse result = commentService.createComment(1L, commentRequest);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBody()).isEqualTo("This is a well-written and detailed post.");

        // Capture the entity passed to save() and verify both mutations fired.
        // any(Comment.class) would silently miss a missing setPost() or setUser() call.
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isEqualTo(post);
        assertThat(captor.getValue().getUser()).isEqualTo(user);

        verify(postRepository).findById(1L);
        verify(securityUtils).getCurrentUser();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during create")
    void createComment_PostNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.createComment(99L, commentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);

        // Post resolution failed — getCurrentUser() and save() must never fire.
        // Fail-fast: no user fetch or persistence when the parent post is absent.
        verify(securityUtils, never()).getCurrentUser();
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // =====================================================================
    // getCommentsByPostId
    //
    // Service calls getPostById(postId) first — post existence is enforced
    // before the comment query fires. findByPostIdWithUser() is the correct
    // repository method (JOIN FETCH on user, avoids N+1 on mapToDto).
    // Empty list is a valid domain state and must return silently.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped CommentResponse list when post has comments")
    void getCommentsByPostId_ReturnsComments() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findByPostIdWithUser(1L))
                .thenReturn(List.of(savedComment));

        // --- ACT ---
        List<CommentResponse> result = commentService.getCommentsByPostId(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getBody()).isEqualTo("This is a well-written and detailed post.");

        verify(postRepository).findById(1L);
        verify(commentRepository).findByPostIdWithUser(1L);
    }

    @Test
    @DisplayName("Should return empty list when post exists but has no comments")
    void getCommentsByPostId_EmptyList() {

        // --- ARRANGE ---

        // A post with zero comments is valid domain state — must return empty list,
        // not throw ResourceNotFoundException.
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findByPostIdWithUser(1L))
                .thenReturn(List.of());

        // --- ACT ---
        List<CommentResponse> result = commentService.getCommentsByPostId(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().isEmpty();

        verify(postRepository).findById(1L);
        verify(commentRepository).findByPostIdWithUser(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during list fetch")
    void getCommentsByPostId_PostNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentsByPostId(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);

        // Post resolution failed — comment query must never fire
        verify(commentRepository, never()).findByPostIdWithUser(anyLong());
    }

    // =====================================================================
    // getCommentById
    //
    // Service enforces that a comment can only be fetched in the context of
    // its own parent post. findByIdWithUser() is the correct repository
    // method (JOIN FETCH on user, avoids LazyInitializationException in mapToDto).
    //
    // Execution paths:
    //   1. Happy path — comment belongs to the post
    //   2. Comment not found — ResourceNotFoundException
    //   3. Ownership violation — comment exists but belongs to a different post
    //
    // Path 3 is the most critical: BlogAPIException must fire before mapToDto().
    // A regression here allows cross-post comment data leakage.
    // =====================================================================

    @Test
    @DisplayName("Should return CommentResponse when comment belongs to the requested post")
    void getCommentById_Success() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // --- ACT ---
        CommentResponse result = commentService.getCommentById(1L, 1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBody()).isEqualTo("This is a well-written and detailed post.");

        verify(commentRepository).findByIdWithUser(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist")
    void getCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentById(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository).findByIdWithUser(99L);
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post")
    void getCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---

        // foreignComment is parented to post.id = 2; request targets post.id = 1.
        // validateCommentBelongsToPost() catches this mismatch and throws BAD_REQUEST.
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(buildForeignComment(user)));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentById(1L, 1L))
                .isInstanceOf(BlogAPIException.class);

        verify(commentRepository).findByIdWithUser(1L);
    }

    // =====================================================================
    // updateCommentById
    //
    // Service sequence: fetch comment → validateCommentBelongsToUser →
    // validateCommentBelongsToPost → mutate body → save.
    //
    // validateCommentBelongsToUser() calls getCurrentUserId() and
    // getCurrentUserAuthorities() on every invocation — both must be stubbed
    // in all update tests that reach this method.
    //
    // ADMIN authority bypasses the ownership check entirely — the ADMIN
    // branch is exercised explicitly to confirm the bypass works.
    //
    // ArgumentCaptor confirms the correct body value was set before save().
    // =====================================================================

    @Test
    @DisplayName("Should update and return CommentResponse when owner updates their own comment")
    void updateCommentById_Success_Owner() {

        // --- ARRANGE ---
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setBody("Updated body — covers additional implementation details.");

        Comment updatedComment = new Comment();
        updatedComment.setId(1L);
        updatedComment.setBody("Updated body — covers additional implementation details.");
        updatedComment.setPost(post);
        updatedComment.setUser(user);

        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // Ownership check: current user is the comment owner (id = 10)
        when(securityUtils.getCurrentUserId())
                .thenReturn(10L);

        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(updatedComment);

        // --- ACT ---
        CommentResponse result = commentService.updateCommentById(1L, 1L, updateRequest);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBody()).isEqualTo("Updated body — covers additional implementation details.");

        // Capture mutated entity — confirms setBody() fired with the correct value
        // before save(). any(Comment.class) would pass even if body was never updated.
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getBody())
                .isEqualTo("Updated body — covers additional implementation details.");

        verify(commentRepository).findByIdWithUser(1L);
        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @DisplayName("Should update comment when ADMIN updates a comment they do not own")
    void updateCommentById_Success_Admin() {

        // --- ARRANGE ---
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setBody("Admin correction applied to this comment.");

        Comment adminUpdatedComment = new Comment();
        adminUpdatedComment.setId(1L);
        adminUpdatedComment.setBody("Admin correction applied to this comment.");
        adminUpdatedComment.setPost(post);
        adminUpdatedComment.setUser(user);

        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // ADMIN user has a different id than the comment owner (10L)
        when(securityUtils.getCurrentUserId())
                .thenReturn(99L);

        // ROLE_ADMIN — validateCommentBelongsToUser() must return early
        // without checking ownership. If the bypass is broken, the id mismatch
        // (99L vs 10L) would cause this test to throw FORBIDDEN.
        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(adminAuthorities());

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(adminUpdatedComment);

        // --- ACT ---
        CommentResponse result = commentService.updateCommentById(1L, 1L, updateRequest);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEqualTo("Admin correction applied to this comment.");

        verify(commentRepository).findByIdWithUser(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw BlogAPIException when non-owner tries to update comment")
    void updateCommentById_Forbidden_NonOwner() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // Current user id (55L) does not match comment owner id (10L)
        when(securityUtils.getCurrentUserId())
                .thenReturn(55L);

        // ROLE_USER — no ADMIN bypass; ownership check must throw FORBIDDEN
        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(1L, 1L, commentRequest))
                .isInstanceOf(BlogAPIException.class);

        verify(commentRepository).findByIdWithUser(1L);
        verify(securityUtils).getCurrentUserId();

        // FORBIDDEN thrown by validateCommentBelongsToUser — save must never fire
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist during update")
    void updateCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(1L, 99L, commentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository).findByIdWithUser(99L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post during update")
    void updateCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---

        // foreignComment is parented to post.id = 2; request targets post.id = 1.
        // Ownership check passes (owner matches) before validateCommentBelongsToPost
        // catches the post mismatch and throws BAD_REQUEST.
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(buildForeignComment(user)));

        // Owner match — validateCommentBelongsToUser passes; post check must throw
        when(securityUtils.getCurrentUserId())
                .thenReturn(10L);

        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(1L, 1L, commentRequest))
                .isInstanceOf(BlogAPIException.class);

        verify(commentRepository).findByIdWithUser(1L);

        // Post ownership violated — save must never fire
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // =====================================================================
    // deleteCommentById
    //
    // Service sequence: fetch comment → validateCommentBelongsToUser →
    // validateCommentBelongsToPost → delete.
    //
    // Same authority-stubbing requirement as update — getCurrentUserId() and
    // getCurrentUserAuthorities() must be stubbed in all tests that reach
    // validateCommentBelongsToUser().
    //
    // verify(commentRepository).delete(savedComment) uses exact reference
    // matching — confirms the correct entity was passed, not just any Comment.
    // =====================================================================

    @Test
    @DisplayName("Should delete comment when owner deletes their own comment")
    void deleteCommentById_Success_Owner() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // Ownership check: current user is the comment owner (id = 10)
        when(securityUtils.getCurrentUserId())
                .thenReturn(10L);

        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        // --- ACT ---
        commentService.deleteCommentById(1L, 1L);

        // --- ASSERT ---
        verify(commentRepository).findByIdWithUser(1L);
        verify(securityUtils).getCurrentUserId();

        // Exact reference match — confirms the fetched entity, not some other
        // Comment instance, was passed to delete()
        verify(commentRepository).delete(savedComment);
    }

    @Test
    @DisplayName("Should delete comment when ADMIN deletes a comment they do not own")
    void deleteCommentById_Success_Admin() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // ADMIN user — different id than the comment owner (10L)
        when(securityUtils.getCurrentUserId())
                .thenReturn(99L);

        // ROLE_ADMIN bypass — if broken, id mismatch (99L vs 10L) throws FORBIDDEN
        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(adminAuthorities());

        // --- ACT ---
        commentService.deleteCommentById(1L, 1L);

        // --- ASSERT ---
        verify(commentRepository).findByIdWithUser(1L);
        verify(commentRepository).delete(savedComment);
    }

    @Test
    @DisplayName("Should throw BlogAPIException when non-owner tries to delete comment")
    void deleteCommentById_Forbidden_NonOwner() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(savedComment));

        // Non-owner, non-admin — ownership check must throw FORBIDDEN
        when(securityUtils.getCurrentUserId())
                .thenReturn(55L);

        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(1L, 1L))
                .isInstanceOf(BlogAPIException.class);

        verify(commentRepository).findByIdWithUser(1L);

        // FORBIDDEN thrown before delete — nothing removed
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist during delete")
    void deleteCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(commentRepository.findByIdWithUser(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository).findByIdWithUser(99L);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post during delete")
    void deleteCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---

        // foreignComment parented to post.id = 2; request targets post.id = 1.
        // Owner matches — validateCommentBelongsToUser passes.
        // validateCommentBelongsToPost catches the mismatch and throws BAD_REQUEST.
        when(commentRepository.findByIdWithUser(1L))
                .thenReturn(Optional.of(buildForeignComment(user)));

        when(securityUtils.getCurrentUserId())
                .thenReturn(10L);

        when(securityUtils.getCurrentUserAuthorities())
                .thenReturn(userAuthorities());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(1L, 1L))
                .isInstanceOf(BlogAPIException.class);

        verify(commentRepository).findByIdWithUser(1L);

        // Post mismatch caught — nothing deleted
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}