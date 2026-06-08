package com.postpulse.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "ValidationFailed",
                                summary = "Validation error",
                                value = """
                                        {
                                          "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#validation-failed",
                                          "title": "Validation Failed",
                                          "status": 400,
                                          "detail": "One or more fields failed validation.",
                                          "instance": "/api/v1/posts",
                                          "timestamp": "2026-05-06T12:00:00Z",
                                          "errors": { "title": "must not be blank" }
                                        }
                                        """
                        )
                )),

        @ApiResponse(responseCode = "401", description = "Authentication required",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "Unauthorized",
                                summary = "Token missing or expired",
                                value = """
                                        {
                                          "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#unauthorized",
                                          "title": "Unauthorized",
                                          "status": 401,
                                          "detail": "Authentication token is missing or has expired.",
                                          "instance": "/api/v1/posts",
                                          "timestamp": "2026-05-06T12:00:00Z"
                                        }
                                        """
                        )
                )),

        @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "Forbidden",
                                summary = "Insufficient permissions",
                                value = """
                                        {
                                          "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#forbidden",
                                          "title": "Forbidden",
                                          "status": 403,
                                          "detail": "You do not have permission to perform this action.",
                                          "instance": "/api/v1/posts/12",
                                          "timestamp": "2026-05-06T12:00:00Z"
                                        }
                                        """
                        )
                )),

        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "NotFound",
                                summary = "Resource not found",
                                value = """
                                        {
                                          "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#not-found",
                                          "title": "Not Found",
                                          "status": 404,
                                          "detail": "Post with id 99 was not found.",
                                          "instance": "/api/v1/posts/99",
                                          "timestamp": "2026-05-06T12:00:00Z"
                                        }
                                        """
                        )
                )),

        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "InternalServerError",
                                summary = "Unexpected server error",
                                value = """
                                        {
                                          "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#internal-server-error",
                                          "title": "Internal Server Error",
                                          "status": 500,
                                          "detail": "An unexpected error occurred. Please try again later.",
                                          "instance": "/api/v1/posts",
                                          "timestamp": "2026-05-06T12:00:00Z"
                                        }
                                        """
                        )
                )),

        @ApiResponse(responseCode = "409", description = "Conflict — duplicate resource",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/schemas/ProblemDetails"),
                        examples = @ExampleObject(
                                name = "Conflict",
                                summary = "Duplicate resource name or slug",
                                value = """
                                {
                                  "type": "https://github.com/AbhishekSharma-99/PostPulse-Backend/blob/main/docs/Errors.md#conflict",
                                  "title": "API Error",
                                  "status": 409,
                                  "detail": "Category with name 'Technology' already exists.",
                                  "instance": "/api/v1/categories",
                                  "timestamp": "2026-05-06T12:00:00Z"
                                }
                                """
                        )
                ))
})
public @interface CommonApiResponses {
}