package com.postpulse.repository;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends BaseRepositoryTest{

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;   // <-- added

    private Category technology;
    private User author;                     // <-- added
    private Post springBootPost;
    private Post hibernatePost;

    @BeforeEach
    void setUp() {
        // 1. Create and save a User (mandatory for Post)
        author = new User();
        author.setUsername("techWriter");
        author.setEmail("writer@example.com");
        author.setPassword("encodedPassword123");
        author = userRepository.save(author);

        // 2. Create and save a Category
        technology = new Category();
        technology.setName("Technology");
        technology.setDescription("Posts about software engineering and tech");
        technology = categoryRepository.save(technology);

        // 3. First Post
        springBootPost = new Post();
        springBootPost.setTitle("Introduction to Spring Boot");
        springBootPost.setDescription("A deep dive into Spring Boot internals");
        springBootPost.setContent("Spring Boot auto-configuration works by scanning the classpath...");
        springBootPost.setSlug("introduction-to-spring-boot");
        springBootPost.setCategory(technology);
        springBootPost.setUser(author);
        springBootPost = postRepository.save(springBootPost);

        // 4. Second Post
        hibernatePost = new Post();
        hibernatePost.setTitle("Hibernate Session Internals");
        hibernatePost.setDescription("How Hibernate manages the persistence context");
        hibernatePost.setContent("Every EntityManager wraps a Hibernate Session...");
        hibernatePost.setSlug("hibernate-session-internals");
        hibernatePost.setCategory(technology);
        hibernatePost.setUser(author);
        hibernatePost = postRepository.save(hibernatePost);
    }

    // =====================================================================
    // findByCategoryId
    // =====================================================================

    @Test
    @DisplayName("findByCategoryId — should return all posts belonging to the given category")
    void findByCategoryId_ReturnsMatchingPosts_WhenCategoryHasPosts() {
        List<Post> result = postRepository.findByCategoryId(technology.getId());
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Post::getSlug)
                .containsExactlyInAnyOrder(
                        "introduction-to-spring-boot",
                        "hibernate-session-internals"
                );
    }

    @Test
    @DisplayName("findByCategoryId — should return empty list when category has no posts")
    void findByCategoryId_ReturnsEmptyList_WhenCategoryHasNoPosts() {
        Category finance = new Category();
        finance.setName("Finance");
        finance.setDescription("Posts about financial markets");
        finance = categoryRepository.save(finance);

        List<Post> result = postRepository.findByCategoryId(finance.getId());
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("findByCategoryId — should not return posts from a different category")
    void findByCategoryId_DoesNotReturnPostsFromOtherCategories() {
        Category devOps = new Category();
        devOps.setName("DevOps");
        devOps.setDescription("Posts about CI/CD and infrastructure");
        devOps = categoryRepository.save(devOps);

        Post dockerPost = new Post();
        dockerPost.setTitle("Docker Networking Explained");
        dockerPost.setDescription("Bridge, host, and overlay networks");
        dockerPost.setContent("Docker uses a virtual bridge by default...");
        dockerPost.setSlug("docker-networking-explained");
        dockerPost.setCategory(devOps);
        dockerPost.setUser(author);   // user is set
        postRepository.save(dockerPost);

        List<Post> result = postRepository.findByCategoryId(technology.getId());
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Post::getSlug)
                .doesNotContain("docker-networking-explained");
    }

    // =====================================================================
    // existsBySlug
    // =====================================================================

    @Test
    @DisplayName("existsBySlug — should return true when a post with that slug exists")
    void existsBySlug_ReturnsTrue_WhenSlugExists() {
        boolean exists = postRepository.existsBySlug("introduction-to-spring-boot");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsBySlug — should return false when no post has that slug")
    void existsBySlug_ReturnsFalse_WhenSlugDoesNotExist() {
        boolean exists = postRepository.existsBySlug("non-existent-slug");
        assertThat(exists).isFalse();
    }

    // =====================================================================
    // existsBySlugAndIdNot
    // =====================================================================

    @Test
    @DisplayName("existsBySlugAndIdNot — should return true when a DIFFERENT post owns the slug")
    void existsBySlugAndIdNot_ReturnsTrue_WhenDifferentPostOwnsSlug() {
        boolean conflict = postRepository.existsBySlugAndIdNot(
                "introduction-to-spring-boot", hibernatePost.getId()
        );
        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("existsBySlugAndIdNot — should return false when the only match is the excluded post (self-reference)")
    void existsBySlugAndIdNot_ReturnsFalse_WhenOnlyMatchIsExcludedPost() {
        boolean conflict = postRepository.existsBySlugAndIdNot(
                "introduction-to-spring-boot", springBootPost.getId()
        );
        assertThat(conflict).isFalse();
    }

    @Test
    @DisplayName("existsBySlugAndIdNot — should return false when the slug does not exist at all")
    void existsBySlugAndIdNot_ReturnsFalse_WhenSlugAbsent() {
        boolean conflict = postRepository.existsBySlugAndIdNot(
                "completely-new-slug", springBootPost.getId()
        );
        assertThat(conflict).isFalse();
    }

    // =====================================================================
    // findBySlug
    // =====================================================================

    @Test
    @DisplayName("findBySlug — should return the post wrapped in Optional when slug exists")
    void findBySlug_ReturnsPost_WhenSlugExists() {
        Optional<Post> result = postRepository.findBySlug("introduction-to-spring-boot");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(springBootPost.getId());
        assertThat(result.get().getTitle()).isEqualTo("Introduction to Spring Boot");
    }

    @Test
    @DisplayName("findBySlug — should return Optional.empty when slug does not exist")
    void findBySlug_ReturnsEmpty_WhenSlugDoesNotExist() {
        Optional<Post> result = postRepository.findBySlug("no-such-slug");
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // deletePostById
    // =====================================================================

    @Test
    @DisplayName("deletePostById — should return 1 and remove the row when the ID exists")
    void deletePostById_ReturnsOne_AndRemovesRow_WhenIdExists() {
        Long idToDelete = springBootPost.getId();
        int rowsAffected = postRepository.deletePostById(idToDelete);
        assertThat(rowsAffected).isEqualTo(1);
        Optional<Post> deleted = postRepository.findById(idToDelete);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("deletePostById — should return 0 when no row matches the given ID")
    void deletePostById_ReturnsZero_WhenIdDoesNotExist() {
        int rowsAffected = postRepository.deletePostById(99999L);
        assertThat(rowsAffected).isEqualTo(0);
    }
}