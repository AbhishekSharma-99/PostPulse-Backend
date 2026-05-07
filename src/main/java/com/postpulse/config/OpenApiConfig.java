package com.postpulse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String BASE_URL = "https://github.com/AbhishekSharma-99";
    private static final String PROBLEM_SCHEMA_REF = "#/components/schemas/ProblemDetails";

    @Bean
    public OpenAPI postPulseOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .externalDocs(buildExternalDocs())
                .components(buildComponents());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("PostPulse REST API")
                .version("1.0.0")
                .description("A blog platform REST API built with Spring Boot, Spring Security and JWT authentication")
                .contact(new Contact()
                        .name("Abhishek Sharma")
                        .email("abhisheksh3397@gmail.com")
                        .url(BASE_URL))
                .license(new License()
                        .name("Apache License, Version 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private ExternalDocumentation buildExternalDocs() {
        return new ExternalDocumentation()
                .description("PostPulse GitHub Repository")
                .url(BASE_URL + "/PostPulse-Backend");
    }

    private Components buildComponents() {
        return new Components()

                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token. Obtain via POST /api/v1/auth/login")
                )

                .addSchemas("ProblemDetails", new ObjectSchema()
                        .addProperty("type", new StringSchema().example(BASE_URL + "/PostPulse-Backend/docs/errors.md#validation-failed"))
                        .addProperty("title", new StringSchema().example("Validation Failed"))
                        .addProperty("status", new IntegerSchema().example(400))
                        .addProperty("detail", new StringSchema().example("One or more fields failed validation."))
                        .addProperty("instance", new StringSchema().example("/api/v1/posts"))
                        .addProperty("timestamp", new StringSchema().example("2026-05-06T12:00:00Z"))
                        .addProperty("errors", new ObjectSchema()
                                .additionalProperties(new StringSchema())
                                .example(Map.of("title", "must not be blank"))
                        )
                )

                .addResponses("BadRequest", new ApiResponse()
                        .description("Invalid request / validation failed")
                        .content(new io.swagger.v3.oas.models.media.Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF)))
                        )
                )

                .addResponses("Unauthorized", new ApiResponse()
                        .description("Authentication required")
                        .content(new io.swagger.v3.oas.models.media.Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF)))
                        )
                )

                .addResponses("Forbidden", new ApiResponse()
                        .description("Access denied")
                        .content(new io.swagger.v3.oas.models.media.Content()
                                .addMediaType("application/ json",
                                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF)))
                        )
                )

                .addResponses("NotFound", new ApiResponse()
                        .description("Resource not found")
                        .content(new io.swagger.v3.oas.models.media.Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF)))
                        )
                )

                .addResponses("InternalServerError", new ApiResponse()
                        .description("Unexpected server error")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF)))
                        )
                );


    }
}