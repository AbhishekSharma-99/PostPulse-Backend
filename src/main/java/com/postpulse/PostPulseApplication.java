package com.postpulse;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "PostPulse REST API",
                version = "1.0.0",
                description = "A blog platform REST API built with Spring Boot, Spring Security and JWT authentication",
                contact = @Contact(
                        name = "Abhishek Sharma",
                        email = "abhisheksh3397@gmail.com",
                        url = "https://github.com/AbhishekSharma-99"
                ),
                license = @License(
                        name = "Apache License, Version 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "PostPulse GitHub Repository",
                url = "https://github.com/AbhishekSharma-99/PostPulse-Backend"
        )
)
public class PostPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostPulseApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}