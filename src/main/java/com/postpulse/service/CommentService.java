package com.postpulse.service;

import com.postpulse.payload.CommentRequest;
import com.postpulse.payload.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(long postId, CommentRequest commentRequest);

    List<CommentResponse> getByPostId(long postId);

    CommentResponse getCommentById(long postId, long commentId);

    CommentResponse updateCommentById(long postId, long commentId, CommentRequest commentRequest);

    void deleteCommentById(long postId, long commentId);

}
