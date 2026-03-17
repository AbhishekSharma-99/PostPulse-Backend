package com.postpulse;

import com.postpulse.entity.Role;
import com.postpulse.repository.RoleRepository;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
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
public class PostPulseApplication implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public PostPulseApplication(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PostPulseApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }
    }
}