package com.postpulse.payload.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Comment response payload returned by API operations")
public class CommentResponse {

    @Schema(description = "Server-assigned comment ID")
    private Long id;

    @Schema(description = "Comment body text")
    private String body;

    @Schema(description = "Username of the commenter")
    private String userName;

    @Schema(description = "Id of the user who made the comment")
    private Long userId;

    @Schema(description = "Id of the post to which the comment belongs")
    private Long postId;
}