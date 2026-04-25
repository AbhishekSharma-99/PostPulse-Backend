package com.postpulse.payload;

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
    private long id;

    @Schema(description = "Display name of the commenter")
    private String name;

    @Schema(description = "Email address of the commenter")
    private String email;

    @Schema(description = "Comment body text")
    private String body;
}