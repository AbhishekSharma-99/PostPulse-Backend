package com.springboot.blog.config;

import com.springboot.blog.security.CustomUserDetailsService;
import com.springboot.blog.security.JwtAuthenticationEntryPoint;
import com.springboot.blog.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Indicates that this class provides Spring configuration
@EnableMethodSecurity // Enables method-level security annotations (e.g., @PreAuthorize)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" // Specifies that the token format is JWT
)
public class SecurityConfig {

    private CustomUserDetailsService customUserDetailsService; // Custom service for user details

    private JwtAuthenticationEntryPoint authenticationEntryPoint; // JWT authentication entry point

    private JwtAuthenticationFilter authenticationFilter; // JWT authentication filter

    // Constructor to inject the custom user details service
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationFilter authenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationFilter = authenticationFilter;
    }

    // Bean to provide a password encoder (BCrypt) for encoding passwords securely
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Returns an instance of BCryptPasswordEncoder
    }

    // Bean to provide the AuthenticationManager for handling authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager(); // Retrieves the authentication manager from the configuration
    }

    // Bean to configure the security filter chain for HTTP requests
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf((httpSecurityCsrfConfigurer) -> httpSecurityCsrfConfigurer.disable()) // Disable CSRF protection
                .authorizeHttpRequests((authorize) -> // Configure authorization for HTTP requests
                        authorize.requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll() // Allow all GET requests to /api/**
                                //.requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .anyRequest().authenticated() // Require authentication for any other request

                ).exceptionHandling(expection -> expection
                        .authenticationEntryPoint(authenticationEntryPoint)
                ).sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build(); // Build and return the security filter chain
    }

    /*// Bean to configure an in-memory user details service with predefined users
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails abhishek = User.builder() // Create a user with username 'abhishek'
                .username("abhishek")
                .password(passwordEncoder().encode("a@123")) // Encode the password
                .roles("ADMIN") // Assign the role 'ADMIN'
                .build();

        UserDetails apurva = User.builder() // Create a user with username 'apurva'
                .username("apurva")
                .password(passwordEncoder().encode("ap@123")) // Encode the password
                .roles("USER") // Assign the role 'USER'
                .build();

        // Return an in-memory user details manager with the defined users
        return new InMemoryUserDetailsManager(abhishek, apurva);
    }*/
}