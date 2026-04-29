package com.postpulse.payload;

public record RegisterResponseDto(
        Long id,
        String username,
        String email,
        String message
) {}
