package com.postpulse.service;

import com.postpulse.payload.comment.CommentRequest;
import com.postpulse.payload.comment.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long postId, CommentRequest commentRequest);

    List<CommentResponse> getCommentsByPostId(Long postId);

    CommentResponse getCommentById(Long postId, Long commentId);

    CommentResponse updateCommentById(Long postId, Long commentId, CommentRequest commentRequest);

    void deleteCommentById(Long postId, Long commentId);

}
