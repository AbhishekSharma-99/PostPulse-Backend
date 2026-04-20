package com.postpulse.service.impl;

import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.payload.CommentDto;
import com.postpulse.repository.CommentRepository;
import com.postpulse.repository.PostRepository;
import com.postpulse.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper mapper;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              ModelMapper mapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CommentDto createComment(long postId, CommentDto commentDto) {
        Post post = getPost(postId);
        Comment comment = mapToEntity(commentDto);
        comment.setPost(post);
        return mapToDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getByPostId(long postId) {
        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CommentDto getCommentById(long postId, long commentId) {
        Post post = getPost(postId);
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, post);
        return mapToDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentById(long postId, long commentId, CommentDto commentDto) {
        Post post = getPost(postId);
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, post);

        comment.setName(commentDto.getName());
        comment.setEmail(commentDto.getEmail());
        comment.setBody(commentDto.getBody());

        return mapToDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentById(long postId, long commentId) {
        Post post = getPost(postId);
        Comment comment = getComment(commentId);
        validateCommentBelongsToPost(comment, post);
        commentRepository.delete(comment);
    }

    // extracted to avoid repeating the same check in 3 methods
    private void validateCommentBelongsToPost(Comment comment, Post post) {
        if (comment.getPost().getId() != post.getId()) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }
    }

    private CommentDto mapToDto(Comment comment) {
        return mapper.map(comment, CommentDto.class);
    }

    private Comment mapToEntity(CommentDto commentDto) {
        return mapper.map(commentDto, Comment.class);
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    private Comment getComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }
}