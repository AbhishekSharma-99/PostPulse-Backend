package com.postpulse.payload.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Comment request payload for create and update operations")
public class CommentRequest {

    @Schema(description = "Comment body text")
    @NotBlank(message = "Body is required")
    @Size(min = 10, message = "Comment body must be at least 10 characters")
    private String body;
}