package com.postpulse.payload.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight post representation used in paginated listings and category views. " +
        "Does not include post body or comments.")
public class PostSummary {

    @Schema(description = "Post ID", example = "12")
    private Long id;

    @Schema(description = "Post title", example = "Getting Started with Spring Boot 3")
    private String title;

    @Schema(description = "Short description for listing cards and SEO", example = "A practical guide to building production-ready APIs")
    private String description;

    @Schema(description = "URL slug for direct navigation", example = "getting-started-with-spring-boot-3")
    private String slug;

    @Schema(description = "Display name of the post author", example = "Abhishek Sharma")
    private String authorName;

    @Schema(description = "Name of the category this post belongs to", example = "Technology")
    private String categoryName;

    @Schema(description = "Timestamp when the post was created (UTC)")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update (UTC)")
    private LocalDateTime updatedAt;
}