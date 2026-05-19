package com.postpulse.payload.post;

import com.postpulse.payload.comment.CommentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full post detail response. Returned by getPostById and getPostBySlug. " +
        "Includes post body, comments, author, category, and audit timestamps.")
public class PostResponse {

    @Schema(description = "Post ID", example = "12")
    private Long id;

    @Schema(description = "Post title", example = "Getting Started with Spring Boot 3")
    private String title;

    @Schema(description = "Short description", example = "A practical guide to building production-ready APIs")
    private String description;

    @Schema(description = "Full post content body")
    private String content;

    @Schema(description = "URL slug", example = "getting-started-with-spring-boot-3")
    private String slug;

    @Schema(description = "Display name of the post author", example = "Abhishek Sharma")
    private String authorName;

    @Schema(description = "ID of the category this post belongs to", example = "3")
    private Long categoryId;

    @Schema(description = "Name of the category this post belongs to", example = "Technology")
    private String categoryName;

    @Schema(description = "All comments on this post, ordered by creation time")
    private List<CommentResponse> comments;

    @Schema(description = "Timestamp when the post was created (UTC)")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update (UTC)")
    private LocalDateTime updatedAt;
}