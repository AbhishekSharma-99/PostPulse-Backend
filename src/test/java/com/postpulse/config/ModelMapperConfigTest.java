package com.postpulse.config;

import com.postpulse.entity.Category;
import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.payload.post.PostResponse;
import com.postpulse.payload.post.PostSummary;
import com.postpulse.utils.TestModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dedicated test for ModelMapper custom mappings.
 * Verifies EVERY field transformation from Post -> PostResponse and Post -> PostSummary.
 * Uses reflection to set audit fields (createdAt/updatedAt) because BaseEntity has no setters.
 */
class ModelMapperConfigTest {

    private ModelMapper modelMapper;
    private Post fullPost;

    @BeforeEach
    void setUp() throws Exception {
        modelMapper = TestModelMapper.getModelMapper();

        // Create associations
        User author = new User();
        author.setId(10L);
        author.setName("Jane Doe");

        Category category = new Category();
        category.setId(5L);
        category.setName("Technology");

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setBody("Great post!");

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setBody("Thanks for sharing");

        fullPost = new Post();
        fullPost.setId(100L);
        fullPost.setTitle("Testing ModelMapper");
        fullPost.setDescription("A deep dive into mapping verification");
        fullPost.setContent("This post proves that all fields are correctly mapped...");
        fullPost.setSlug("testing-modelmapper");
        fullPost.setUser(author);
        fullPost.setCategory(category);
        fullPost.setComments(List.of(comment1, comment2));

        // Use reflection to set audit fields (BaseEntity has private fields without setters)
        setAuditField(fullPost, "createdAt", LocalDateTime.parse("2024-01-01T10:00:00"));
        setAuditField(fullPost, "updatedAt", LocalDateTime.parse("2024-01-02T12:30:00"));
    }

    private void setAuditField(Post post, String fieldName, Object value) throws Exception {
        Field field = post.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(post, value);
    }

    @Test
    void postToResponseDto_mapsAllFieldsCorrectly() {
        PostResponse dto = modelMapper.map(fullPost, PostResponse.class);

        // Direct fields
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Testing ModelMapper");
        assertThat(dto.getDescription()).isEqualTo("A deep dive into mapping verification");
        assertThat(dto.getContent()).isEqualTo("This post proves that all fields are correctly mapped...");
        assertThat(dto.getSlug()).isEqualTo("testing-modelmapper");

        // Nested associations (via custom PropertyMap)
        assertThat(dto.getAuthorName()).isEqualTo("Jane Doe");
        assertThat(dto.getCategoryId()).isEqualTo(5L);
        assertThat(dto.getCategoryName()).isEqualTo("Technology");

        // Collection – ensure the list is present (but we don't test inner mapping here)
        assertThat(dto.getComments()).hasSize(2);

        // Audit timestamps (copied from BaseEntity fields)
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.parse("2024-01-01T10:00:00"));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.parse("2024-01-02T12:30:00"));
    }

    @Test
    void postToSummaryDto_mapsAllFieldsCorrectly() {
        PostSummary dto = modelMapper.map(fullPost, PostSummary.class);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Testing ModelMapper");
        assertThat(dto.getDescription()).isEqualTo("A deep dive into mapping verification");
        assertThat(dto.getSlug()).isEqualTo("testing-modelmapper");
        assertThat(dto.getAuthorName()).isEqualTo("Jane Doe");
        assertThat(dto.getCategoryName()).isEqualTo("Technology");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.parse("2024-01-01T10:00:00"));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.parse("2024-01-02T12:30:00"));
    }

    @Test
    void handlesNullRelationsGracefully(){
        // Remove associations
        fullPost.setUser(null);
        fullPost.setCategory(null);
        fullPost.setComments(null);
        // Reset audit fields to non-null to avoid confusion; they stay as set.

        PostResponse response = modelMapper.map(fullPost, PostResponse.class);
        assertThat(response.getAuthorName()).isNull();
        assertThat(response.getCategoryId()).isNull();
        assertThat(response.getCategoryName()).isNull();
        assertThat(response.getComments()).isNull();

        PostSummary summary = modelMapper.map(fullPost, PostSummary.class);
        assertThat(summary.getAuthorName()).isNull();
        assertThat(summary.getCategoryName()).isNull();
    }
}