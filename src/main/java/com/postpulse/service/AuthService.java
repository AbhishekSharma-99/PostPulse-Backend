package com.postpulse.service;

import com.postpulse.payload.LoginDto;
import com.postpulse.payload.RegisterDto;
import com.postpulse.payload.RegisterResponseDto;

public interface AuthService {
    String login(LoginDto loginDto);

    RegisterResponseDto register(RegisterDto registerDto);
}
