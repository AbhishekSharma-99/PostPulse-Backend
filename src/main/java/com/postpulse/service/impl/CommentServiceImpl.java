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
import com.postpulse.service.CommentService;
import com.postpulse.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              ModelMapper modelMapper,
                              SecurityUtils securityUtils) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest commentRequest) {
        // Fetch all required dependencies first (fail-fast principle)
        Post post = getPostById(postId);
        User user = securityUtils.getCurrentUser();

        // Map and assemble the entity
        Comment comment = mapToEntity(commentRequest);
        comment.setPost(post);
        comment.setUser(user);

        // Save and return response
        Comment savedComment = commentRepository.save(comment);
        log.info("Created comment with id: {} for post id: {} by user: {}",
                savedComment.getId(), postId, user.getUsername());
        return mapToDto(savedComment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // Verify post exists first (optional but good practice)
        getPostById(postId);

        return commentRepository.findByPostIdWithUser(postId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CommentResponse getCommentById(Long postId, Long commentId) {
        Comment comment = getCommentByCommentId(commentId);
        validateCommentBelongsToPost(comment, postId);
        return mapToDto(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateCommentById(Long postId, Long commentId, CommentRequest commentRequest) {
        Comment comment = getCommentByCommentId(commentId);
        validateCommentBelongsToUser(comment);
        validateCommentBelongsToPost(comment, postId);

        comment.setBody(commentRequest.getBody());

        Comment updatedComment = commentRepository.save(comment);
        log.info("Updated comment with id: {} for post id: {}", commentId, postId);
        return mapToDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long postId, Long commentId) {
        Comment comment = getCommentByCommentId(commentId);
        validateCommentBelongsToUser(comment);
        validateCommentBelongsToPost(comment, postId);

        commentRepository.delete(comment);
        log.info("Deleted comment with id: {} from post id: {}", commentId, postId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates that a comment belongs to the User.
     *
     * @param comment the comment to validate
     * @throws BlogAPIException if the comment does not belong to the post
     */
    private void validateCommentBelongsToUser(Comment comment) {
        long userId = securityUtils.getCurrentUserId();

        boolean isAdmin = securityUtils.getCurrentUserAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return; // ADMIN bypasses ownership check

        if(!comment.getUser().getId().equals(userId)) {
            throw new BlogAPIException(
                    HttpStatus.FORBIDDEN,
                    String.format("User with id %d is not authorized to modify comment with id %d",
                            userId, comment.getId())
            );
        }
    }

    /**
     * Validates that a comment belongs to the specified post.
     *
     * @param comment the comment to validate
     * @param postId the post to check against
     * @throws BlogAPIException if the comment does not belong to the post
     */
    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new BlogAPIException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Comment with id %d does not belong to post with id %d",
                            comment.getId(), postId)
            );
        }
    }

    /**
     * Maps Comment entity to CommentResponse DTO.
     * Includes username from the associated User entity.
     */
    private CommentResponse mapToDto(Comment comment) {
        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        response.setUserName(comment.getUser().getUsername());
        return response;
    }

    /**
     * Maps CommentRequest DTO to Comment entity.
     */
    private Comment mapToEntity(CommentRequest commentRequest) {
        return modelMapper.map(commentRequest, Comment.class);
    }

    /**
     * Retrieves a post by ID or throws ResourceNotFoundException.
     */
    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    /**
     * Retrieves a comment by ID or throws ResourceNotFoundException.
     */
    private Comment getCommentByCommentId(Long commentId) {
        return commentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }
}