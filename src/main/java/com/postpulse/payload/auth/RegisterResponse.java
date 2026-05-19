package com.postpulse.payload.auth;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String message
) {}
