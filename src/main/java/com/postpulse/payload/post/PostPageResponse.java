package com.postpulse.payload.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper for post listings. " +
        "Contains a page of lightweight PostSummaryDto items plus pagination metadata.")
public class PostPageResponse {

    @Schema(description = "Post summaries for the current page")
    private List<PostSummary> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNo;

    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of posts across all pages", example = "47")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "True if this is the last page", example = "false")
    private boolean last;
}