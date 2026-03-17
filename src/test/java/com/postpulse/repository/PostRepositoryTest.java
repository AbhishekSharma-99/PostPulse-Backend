package com.postpulse.repository;

import com.postpulse.repository.PostRepository;
import com.postpulse.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void savePost_ShouldPersistPost() {
        // Given
        Post post = new Post();
        post.setTitle("Test Title");
        post.setDescription("Test Description");
        post.setContent("Test Content");

        // When
        Post savedPost = postRepository.save(post);

        // Then
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getId()).isGreaterThan(0);
        assertThat(savedPost.getTitle()).isEqualTo("Test Title");
    }

    @Test
    void findById_ShouldReturnPost_WhenExists() {
        // Given
        Post post = new Post();
        post.setTitle("Test Title");
        post.setDescription("Test Description");
        post.setContent("Test Content");
        Post savedPost = postRepository.save(post);

        // When
        var foundPost = postRepository.findById(savedPost.getId());

        // Then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    void findAll_ShouldReturnAllPosts() {
        // Given
        Post post1 = new Post();
        post1.setTitle("First Post");
        post1.setDescription("First Description");
        post1.setContent("First Content");
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Second Post");
        post2.setDescription("Second Description");
        post2.setContent("Second Content");
        postRepository.save(post2);

        // When
        Page<Post> posts = postRepository.findAll(PageRequest.of(0, 10));

        // Then
        assertThat(posts.getContent()).hasSize(2);
        assertThat(posts).extracting("title").contains("First Post", "Second Post");
    }

    @Test
    void updatePost_ShouldUpdatePostData() {
        // Given
        Post post = new Post();
        post.setTitle("Test Title");
        post.setDescription("Test Description");
        post.setContent("Test Content");
        Post savedPost = postRepository.save(post);

        // When
        savedPost.setTitle("Updated Title");
        Post updatedPost = postRepository.save(savedPost);

        // Then
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void deletePost_ShouldRemovePost() {
        // Given
        Post post = new Post();
        post.setTitle("Test Title");
        post.setDescription("Test Description");
        post.setContent("Test Content");
        Post savedPost = postRepository.save(post);

        // When
        postRepository.delete(savedPost);
        var deletedPost = postRepository.findById(savedPost.getId());

        // Then
        assertThat(deletedPost).isEmpty();
    }

    @Test
    void findById_ShouldReturnEmpty_WhenPostDoesNotExist() {
        // When
        var notFoundPost = postRepository.findById(999L);

        // Then
        assertThat(notFoundPost).isEmpty();
    }
}
