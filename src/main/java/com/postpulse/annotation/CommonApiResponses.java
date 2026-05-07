package com.postpulse.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetails"))),

        @ApiResponse(responseCode = "401", description = "Authentication required",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetails"))),

        @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetails"))),

        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetails"))),

        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ProblemDetails")))
})
public @interface CommonApiResponses {
}