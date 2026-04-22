package com.postpulse.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "CommentDto Model Information")
public class CommentDto {

    @Schema(description = "Comment ID")
    private long id;

    @Schema(description = "Comment Name")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Comment Email")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(description = "Comment Body")
    @NotBlank(message = "Body is required")
    @Size(min = 10, message = "Comment body must be at least 10 characters")
    private String body;
}