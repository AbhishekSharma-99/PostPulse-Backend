package com.postpulse.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Post response payload")
public class PostResponseDto {

    @Schema(description = "Blog post ID")
    private long id;

    @Schema(description = "Blog post Title")
    private String title;

    @Schema(description = "Blog post Description")
    private String description;

    @Schema(description = "Blog post Content")
    private String content;

    @Schema(description = "Blog post Comments")
    private Set<CommentDto> comments;

    @Schema(description = "Blog post Category ID")
    private long categoryId;
}