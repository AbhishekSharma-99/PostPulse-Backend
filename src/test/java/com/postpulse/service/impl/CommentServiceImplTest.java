package com.postpulse.service.impl;

import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.CommentRequest;
import com.postpulse.payload.CommentResponse;
import com.postpulse.repository.CommentRepository;
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

import java.util.List;
import java.util.Optional;

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

    // @Spy wraps a real ModelMapper instance — actual mapping logic executes.
    // Comment has no custom PropertyMap (unlike Post), so default field-name
    // matching covers all fields. A @Mock would only prove map() was invoked,
    // never that id/name/email/body were actually populated correctly.
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CommentServiceImpl commentService;

    private Post post;
    private Comment savedComment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1L);
        post.setTitle("Spring Boot Internals");
        post.setDescription("Deep dive into Spring Boot auto-configuration");
        post.setContent("Full content here...");

        // CommentRequest — incoming payload; no id field by design
        commentRequest = new CommentRequest();
        commentRequest.setName("Alice");
        commentRequest.setEmail("alice@example.com");
        commentRequest.setBody("This is a well-written and detailed post.");

        // savedComment — what the repository returns after save();
        // post must be set so validateCommentBelongsToPost() can traverse
        // comment.getPost().getId() without NullPointerException
        savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setName("Alice");
        savedComment.setEmail("alice@example.com");
        savedComment.setBody("This is a well-written and detailed post.");
        savedComment.setPost(post);
    }

    // =====================================================================
    // Static fixture factory — shared across all three ownership-violation tests
    //
    // static because it references no instance state (@BeforeEach fields).
    // Its only job is to produce a Comment whose parent post id differs from
    // the post id used in the request — that mismatch is what triggers
    // validateCommentBelongsToPost() to throw BlogAPIException.
    //
    // All three ownership tests (getCommentById, updateCommentById,
    // deleteCommentById) need exactly this scenario, so extracting it
    // eliminates three identical blocks of inline setup.
    // =====================================================================

    private static Comment buildForeignComment() {
        Post otherPost = new Post();
        otherPost.setId(2L);  // different from post.id = 1 used in all requests

        Comment foreignComment = new Comment();
        foreignComment.setId(1L);
        foreignComment.setName("Alice");
        foreignComment.setEmail("alice@example.com");
        foreignComment.setBody("This is a well-written and detailed post.");
        foreignComment.setPost(otherPost);  // parented to post 2 — request targets post 1
        return foreignComment;
    }

    // =====================================================================
    // createComment
    //
    // The service maps CommentRequest -> Comment, then mutates the entity
    // by calling comment.setPost(post) before persisting. ArgumentCaptor
    // captures that mutated state and lets us assert the correct post
    // reference was injected — any(Comment.class) would silently miss this.
    // Real ModelMapper runs and verifies the field mapping is correct.
    // =====================================================================

    @Test
    @DisplayName("Should create and return CommentResponse when post exists")
    void createComment_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        // --- ACT ---
        CommentResponse result = commentService.createComment(1L, commentRequest);

        // --- ASSERT ---

        // Real ModelMapper ran — these values come from actual field mapping,
        // not a stub. If the mapping ever breaks, these assertions catch it.
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getBody()).isEqualTo("This is a well-written and detailed post.");

        // Capture the entity passed to save() and verify setPost() was called —
        // this is the only mutation inside createComment() that isn't visible
        // from the method signature. any(Comment.class) would silently miss it.
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isEqualTo(post);

        verify(postRepository).findById(1L);
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

        // Nothing must be persisted when the parent post doesn't exist —
        // exception alone is not sufficient evidence; save() must never fire
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // =====================================================================
    // getByPostId
    //
    // findByPostId() takes a long — exact argument matching confirms the
    // service passed the correct postId downstream.
    // Empty list is valid domain state (a post with no comments yet)
    // and must return silently, not throw ResourceNotFoundException.
    // =====================================================================

    @Test
    @DisplayName("Should return mapped CommentResponse list when post has comments")
    void getByPostId_ReturnsComments() {

        // --- ARRANGE ---
        when(commentRepository.findByPostId(1L))
                .thenReturn(List.of(savedComment));

        // --- ACT ---
        List<CommentResponse> result = commentService.getByPostId(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("Alice");
        assertThat(result.getFirst().getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getFirst().getBody()).isEqualTo("This is a well-written and detailed post.");

        verify(commentRepository).findByPostId(1L);
    }

    @Test
    @DisplayName("Should return empty list when post has no comments")
    void getByPostId_EmptyList() {

        // --- ARRANGE ---

        // A post with zero comments is valid domain state — the method must
        // return an empty list, not throw. findByPostId() returns empty for
        // both "post exists but has no comments" and "post doesn't exist" —
        // the service treats both identically and that's intentional here.
        when(commentRepository.findByPostId(1L))
                .thenReturn(List.of());

        // --- ACT ---
        List<CommentResponse> result = commentService.getByPostId(1L);

        // --- ASSERT ---
        assertThat(result).isNotNull().isEmpty();

        verify(commentRepository).findByPostId(1L);
    }

    // =====================================================================
    // getCommentById
    //
    // The service enforces that a comment can only be fetched in the context
    // of its own parent post. Four distinct execution paths:
    //   1. Happy path — comment belongs to the post
    //   2. Post not found — comment fetch must be skipped entirely
    //   3. Comment not found — after post resolves successfully
    //   4. Ownership violation — comment exists but belongs to a different post
    //
    // Path 4 is the most critical: BlogAPIException must fire before mapToDto().
    // A regression here would allow cross-post comment data leakage.
    // =====================================================================

    @Test
    @DisplayName("Should return CommentResponse when comment belongs to the requested post")
    void getCommentById_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(savedComment));

        // --- ACT ---
        CommentResponse result = commentService.getCommentById(1L, 1L);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getBody()).isEqualTo("This is a well-written and detailed post.");

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist")
    void getCommentById_PostNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);

        // Post resolution failed — comment fetch must never be attempted
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist")
    void getCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentById(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(99L);
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post")
    void getCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(buildForeignComment()));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.getCommentById(1L, 1L))
                .isInstanceOf(BlogAPIException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);
    }

    // =====================================================================
    // updateCommentById
    //
    // The service mutates the fetched entity in-place (setName, setEmail,
    // setBody) before calling save(). ArgumentCaptor captures the exact
    // state passed to save() and asserts each field individually.
    // This catches regressions where the service forgets one setter —
    // any(Comment.class) would silently pass that through.
    // =====================================================================

    @Test
    @DisplayName("Should update and return CommentResponse when comment belongs to the given post")
    void updateCommentById_Success() {

        // --- ARRANGE ---
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setName("Alice Smith");
        updateRequest.setEmail("alice.smith@example.com");
        updateRequest.setBody("Updated body — this covers additional implementation details.");

        Comment updatedComment = new Comment();
        updatedComment.setId(1L);
        updatedComment.setName("Alice Smith");
        updatedComment.setEmail("alice.smith@example.com");
        updatedComment.setBody("Updated body — this covers additional implementation details.");
        updatedComment.setPost(post);

        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(savedComment));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(updatedComment);

        // --- ACT ---
        CommentResponse result = commentService.updateCommentById(1L, 1L, updateRequest);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alice Smith");
        assertThat(result.getEmail()).isEqualTo("alice.smith@example.com");
        assertThat(result.getBody()).isEqualTo("Updated body — this covers additional implementation details.");

        // Capture the mutated entity and assert all three fields were set —
        // if the service ever forgets one setter, any(Comment.class) passes
        // silently while this captor catches the stale field value
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("Alice Smith");
        assertThat(captured.getEmail()).isEqualTo("alice.smith@example.com");
        assertThat(captured.getBody()).isEqualTo("Updated body — this covers additional implementation details.");

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during update")
    void updateCommentById_PostNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(99L, 1L, commentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);
        verify(commentRepository, never()).findById(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist during update")
    void updateCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(1L, 99L, commentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(99L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post during update")
    void updateCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(buildForeignComment()));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.updateCommentById(1L, 1L, commentRequest))
                .isInstanceOf(BlogAPIException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);

        // Ownership check must short-circuit before any persistence attempt
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // =====================================================================
    // deleteCommentById
    //
    // No mutation happens — service fetches, validates ownership, then deletes.
    // verify(commentRepository).delete(savedComment) uses exact reference
    // matching to confirm the correct entity was passed to delete(), not just
    // any Comment that happened to be in scope.
    // =====================================================================

    @Test
    @DisplayName("Should delete comment when it belongs to the given post")
    void deleteCommentById_Success() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(savedComment));

        // --- ACT ---
        commentService.deleteCommentById(1L, 1L);

        // --- ASSERT ---
        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);

        // Exact reference match — confirms the fetched entity, not a random Comment,
        // was passed to delete(). any(Comment.class) would pass even if the wrong
        // entity was deleted.
        verify(commentRepository).delete(savedComment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when post does not exist during delete")
    void deleteCommentById_PostNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(99L);
        verify(commentRepository, never()).findById(anyLong());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when comment does not exist during delete")
    void deleteCommentById_CommentNotFound() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(99L);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Should throw BlogAPIException when comment belongs to a different post during delete")
    void deleteCommentById_CommentDoesNotBelongToPost() {

        // --- ARRANGE ---
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(post));

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(buildForeignComment()));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> commentService.deleteCommentById(1L, 1L))
                .isInstanceOf(BlogAPIException.class);

        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);

        // Ownership check must block deletion — nothing removed on mismatch
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}