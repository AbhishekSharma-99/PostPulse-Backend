package com.springboot.blog.payload;

import com.springboot.blog.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(
        description = "PostDto Model Information "
)
public class PostDto {
    @Schema(
            description = "Blog post ID"
    )
    private long id;

    @Schema(
            description = "Blog post Title"
    )
    //title should not be null or empty
    //title should have at least 2 characters
    @NotEmpty
    @Size(min = 2, message = "Post title should have at least 2 characters")
    private String title;

    @Schema(
            description = "Blog post Description"
    )
    //description should not be null or empty
    //description should have at least 10 characters
    @NotEmpty
    @Size(min = 10, message = "Post description should have at least 10 charracters")
    private String description;

    @Schema(
            description = "Blog post Content"
    )
    @NotEmpty
    private String content;

    @Schema(
            description = "Blog post Comments"
    )
    private Set<CommentDto> comments;

    @Schema(
            description = "Blog post Category ID"
    )
    private long categoryId;

}
