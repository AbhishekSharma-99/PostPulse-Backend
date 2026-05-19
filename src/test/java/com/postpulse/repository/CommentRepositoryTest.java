package com.postpulse.repository;

import com.postpulse.entity.Category;
import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ─── Shared Fixtures ────────────────────────────────────────────────────
    private Category defaultCategory;
    private Post postA;
    private User author;
    private Comment commentOnA1;
    private Comment commentOnB;

    @BeforeEach
    void setUp() {
        defaultCategory = createCategory();
        author = createUser();

        postA = createPost("Spring Boot Internals", "Deep dive into Spring Boot auto-configuration",
                "Spring Boot scans the classpath for @Configuration classes...",
                "spring-boot-internals", defaultCategory, author);

        Post postB = createPost("JPA Persistence Context", "How Hibernate manages entity state",
                "The persistence context is a first-level cache...",
                "jpa-persistence-context", defaultCategory, author);

        commentOnA1 = createComment("Great breakdown of auto-configuration!", postA, author);
        Comment commentOnA2 = createComment("The classpath scanning explanation really helped.", postA, author);
        commentOnB = createComment("Finally understand the first-level cache!", postB, author);
    }

    // ─── Helper methods for entity creation ────────────────────────────────
    private Category createCategory() {
        Category category = new Category();
        category.setName("General");
        category.setDescription("General topics and discussions");
        return categoryRepository.save(category);
    }

    private User createUser() {
        User user = new User();
        user.setName("Abhishek Sharma");
        user.setUsername("abhishek");
        user.setEmail("abhishek@postpulse.com");
        user.setPassword("hashed_password");
        return userRepository.save(user);
    }

    private Post createPost(String title, String description, String content, String slug,
                            Category category, User user) {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setContent(content);
        post.setSlug(slug);
        post.setCategory(category);
        post.setUser(user);
        return postRepository.save(post);
    }

    private Comment createComment(String body, Post post, User user) {
        Comment comment = new Comment();
        comment.setBody(body);
        comment.setPost(post);
        comment.setUser(user);
        return commentRepository.save(comment);
    }

    // =====================================================================
    // findByPostId
    // Derived query — SELECT * FROM comments WHERE post_id = ?
    // =====================================================================

    @Test
    @DisplayName("findByPostId — should return all comments belonging to the given post")
    void findByPostId_ReturnsMatchingComments_WhenPostHasComments() {
        List<Comment> result = commentRepository.findByPostId(postA.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Comment::getBody)
                .containsExactlyInAnyOrder(
                        "Great breakdown of auto-configuration!",
                        "The classpath scanning explanation really helped."
                );
    }

    @Test
    @DisplayName("findByPostId — should return empty list when post has no comments")
    void findByPostId_ReturnsEmptyList_WhenPostHasNoComments() {
        // Create a post without any comments
        Post emptyPost = createPost("No Comments", "Description", "Content",
                "no-comments-post", defaultCategory, author);

        List<Comment> result = commentRepository.findByPostId(emptyPost.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByPostId — should not return comments belonging to a different post")
    void findByPostId_DoesNotReturnCommentsFromOtherPosts() {
        List<Comment> result = commentRepository.findByPostId(postA.getId());

        assertThat(result).extracting(Comment::getBody)
                .doesNotContain(commentOnB.getBody());
    }

    // =====================================================================
    // findByPostIdWithUser — JOIN FETCH
    // JPQL: SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId
    // =====================================================================

    @Test
    @DisplayName("findByPostIdWithUser — should return comments with user eagerly loaded")
    void findByPostIdWithUser_ReturnsCommentsWithUserPopulated() {
        List<Comment> result = commentRepository.findByPostIdWithUser(postA.getId());

        assertThat(result).hasSize(2);
        result.forEach(comment -> assertThat(comment.getUser())
                .as("User should be eagerly loaded (JOIN FETCH) and not a proxy")
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(author.getId());
                    assertThat(user.getUsername()).isEqualTo("abhishek");
                }));
    }

    @Test
    @DisplayName("findByPostIdWithUser — should return empty list when no comments exist for the post")
    void findByPostIdWithUser_ReturnsEmptyList_WhenNoCommentsExist() {
        Post silentPost = createPost("Redis Caching", "Cache-aside pattern with Redis",
                "Redis stores data in memory with optional persistence...",
                "redis-caching", defaultCategory, author);

        List<Comment> result = commentRepository.findByPostIdWithUser(silentPost.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByPostIdWithUser — should not return comments from other posts")
    void findByPostIdWithUser_DoesNotReturnCommentsFromOtherPosts() {
        List<Comment> result = commentRepository.findByPostIdWithUser(postA.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Comment::getBody)
                .doesNotContain("Finally understand the first-level cache!");
    }

    // =====================================================================
    // findByIdWithUser — JOIN FETCH
    // JPQL: SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :commentId
    // =====================================================================

    @Test
    @DisplayName("findByIdWithUser — should return comment with user eagerly loaded when ID exists")
    void findByIdWithUser_ReturnsCommentWithUserPopulated_WhenIdExists() {
        Optional<Comment> result = commentRepository.findByIdWithUser(commentOnA1.getId());

        assertThat(result).isPresent();
        Comment comment = result.get();
        assertThat(comment.getBody()).isEqualTo("Great breakdown of auto-configuration!");
        assertThat(comment.getUser())
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(author.getId());
                    assertThat(user.getUsername()).isEqualTo("abhishek");
                });
    }

    @Test
    @DisplayName("findByIdWithUser — should return Optional.empty when comment ID does not exist")
    void findByIdWithUser_ReturnsEmpty_WhenIdDoesNotExist() {
        Optional<Comment> result = commentRepository.findByIdWithUser(99999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdWithUser — should return empty for a deleted comment ID")
    void findByIdWithUser_ReturnsEmpty_ForDeletedComment() {
        // Delete an existing comment and ensure it's no longer found
        commentRepository.deleteById(commentOnB.getId());
        Optional<Comment> result = commentRepository.findByIdWithUser(commentOnB.getId());
        assertThat(result).isEmpty();
    }
}