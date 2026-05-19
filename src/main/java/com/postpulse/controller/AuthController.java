package com.postpulse.controller;

import com.postpulse.annotation.CommonApiResponses;
import com.postpulse.payload.auth.JwtAuthResponse;
import com.postpulse.payload.auth.LoginRequest;
import com.postpulse.payload.auth.RegisterRequest;
import com.postpulse.payload.auth.RegisterResponse;
import com.postpulse.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "Sign-in/Sign-up REST APIs for Auth Resource")
@CommonApiResponses
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login", description = "Authenticate user with provided credentials")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok(new JwtAuthResponse(token));
    }

    @Operation(summary = "Register", description = "Register new user with provided credentials")
    @ApiResponse(responseCode = "201", description = "Registration successful")
    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }
}