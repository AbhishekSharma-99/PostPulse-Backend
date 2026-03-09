package com.springboot.blog.repository;

import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        // Create and save a post for testing comments
        post = new Post();
        post.setTitle("Test Post");
        post.setDescription("Test Description");
        post.setContent("Test Content");
        postRepository.save(post);
    }

    @Test
    void saveComment_ShouldPersistComment() {
        // Given
        Comment comment = new Comment();
        comment.setName("John Doe");
        comment.setEmail("john@example.com");
        comment.setBody("Great post!");
        comment.setPost(post);

        // When
        Comment savedComment = commentRepository.save(comment);

        // Then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isGreaterThan(0);
        assertThat(savedComment.getName()).isEqualTo("John Doe");
    }

    @Test
    void findById_ShouldReturnComment_WhenExists() {
        // Given
        Comment comment = new Comment();
        comment.setName("John Doe");
        comment.setEmail("john@example.com");
        comment.setBody("Great post!");
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);

        // When
        var foundComment = commentRepository.findById(savedComment.getId());

        // Then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByPostId_ShouldReturnCommentsForPost() {
        // Given
        Comment comment1 = new Comment();
        comment1.setName("John Doe");
        comment1.setEmail("john@example.com");
        comment1.setBody("Great post!");
        comment1.setPost(post);
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setName("Jane Doe");
        comment2.setEmail("jane@example.com");
        comment2.setBody("Nice article!");
        comment2.setPost(post);
        commentRepository.save(comment2);

        // When
        var comments = commentRepository.findByPostId(post.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("name").contains("John Doe", "Jane Doe");
    }

    @Test
    void updateComment_ShouldUpdateCommentData() {
        // Given
        Comment comment = new Comment();
        comment.setName("John Doe");
        comment.setEmail("john@example.com");
        comment.setBody("Great post!");
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);

        // When
        savedComment.setName("Updated Name");
        Comment updatedComment = commentRepository.save(savedComment);

        // Then
        assertThat(updatedComment.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteComment_ShouldRemoveComment() {
        // Given
        Comment comment = new Comment();
        comment.setName("John Doe");
        comment.setEmail("john@example.com");
        comment.setBody("Great post!");
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);

        // When
        commentRepository.delete(savedComment);
        var deletedComment = commentRepository.findById(savedComment.getId());

        // Then
        assertThat(deletedComment).isEmpty();
    }
}
