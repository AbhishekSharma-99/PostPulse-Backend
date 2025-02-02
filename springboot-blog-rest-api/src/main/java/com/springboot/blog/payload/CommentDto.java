package com.springboot.blog.payload;

import com.springboot.blog.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(
        description = "CommentDto Model Information"
)
public class CommentDto {
    @Schema(
            description = "Comment ID"
    )
    private long id;
    @Schema(
            description = "Comment Name"
    )
    private String name;
    @Schema(
            description = "Comment Email"
    )
    private String email;
    @Schema(
            description = "Comment Body"
    )
    private String body;
}
