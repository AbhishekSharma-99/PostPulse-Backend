package com.springboot.blog.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PostResponse Model Information")
public class PostResponse {
    @Schema(description = "List of Posts")
    private List<PostDto> content;
    @Schema(description = "Page number")
    private int pageNo;
    @Schema(description = "Page size")
    private int pageSize;
    @Schema(description = "Total number of posts")
    private long totalElement;
    @Schema(description = "Total number of pages")
    private int totalPages;
    @Schema(description = "Is it the last page")
    private boolean last;


}
