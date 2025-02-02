package com.springboot.blog.service.impl;

import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.BlogAPIExecution;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.repository.CommentRepository;
import com.springboot.blog.repository.PostRepository;
import com.springboot.blog.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ModelMapper mapper;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, ModelMapper mapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.mapper = mapper;
    }

    @Override
    public CommentDto createComment(long postId, CommentDto commentDto) {
        Comment comment = mapToEntity(commentDto);

        //retrieving  post from database
        Post post = getPost(postId);

        //set post to comment
        comment.setPost(post);

        //comment entity to db
        Comment newCommnet = commentRepository.save(comment);

        return mapToDto(newCommnet);
    }

    @Override
    public List<CommentDto> getByPostId(long postId) {

        //retrieving comments from database
        List<Comment> comments = commentRepository.findByPostId(postId);

        //converting the list of comments to list of dto's
        return comments.stream().map(comment -> mapToDto(comment)).collect(Collectors.toList()) ;
    }

    @Override
    public CommentDto getCommentById(long postId, long commentId) {

        //retrieving  post from database using private method
        Post post = getPost(postId);

        //retrieving comments from database using private method
        Comment comment = getComment(commentId);

        //check if the comment belongs to the same post it's being asked for
        if (!(comment.getPost().equals(post))) {
            throw new BlogAPIExecution(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        return mapToDto(comment);
    }

    @Override
    public CommentDto updateCommentById(long postId, long commentId, CommentDto commentDto) {

        //retrieving  post from database
        Post post = getPost(postId);

        //retrieving comments from database
        Comment comment = getComment(commentId);

        //check if the comment belongs to the same post it's being asked for
        if (!(comment.getPost().equals(post))) {
            throw new BlogAPIExecution(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        comment.setName(commentDto.getName());
        comment.setEmail(commentDto.getEmail());
        comment.setBody(commentDto.getBody());

        Comment updatedComment = commentRepository.save(comment);

        return mapToDto(updatedComment);
    }



    @Override
    public void deleteCommentById(long postId, long commentId) {

        Post post = getPost(postId);

        //retrieving comments from database
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        //check if the comment belongs to the same post it's being asked for
        if (!(comment.getPost().equals(post))) {
            throw new BlogAPIExecution(HttpStatus.BAD_REQUEST, "Comment does not belong to post");
        }

        commentRepository.delete(comment);
    }

    private CommentDto mapToDto(Comment comment){
        CommentDto commentDto = mapper.map(comment, CommentDto.class);

//        CommentDto commentDto = new CommentDto();
//        commentDto.setId(comment.getId());
//        commentDto.setName(comment.getName());
//        commentDto.setEmail(comment.getEmail());
//        commentDto.setBody(comment.getBody());
        return commentDto;
    }

    private Comment mapToEntity(CommentDto commentDto){
        Comment comment = mapper.map(commentDto, Comment.class);

//        Comment comment = new Comment();
//        comment.setId(commentDto.getId());
//        comment.setName(commentDto.getName());
//        comment.setEmail(commentDto.getEmail());
//        comment.setBody(commentDto.getBody());
        return comment;
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    private Comment getComment(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }
}
