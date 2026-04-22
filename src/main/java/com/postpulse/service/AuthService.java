package com.postpulse.service;

import com.postpulse.payload.LoginDto;
import com.postpulse.payload.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);

    void register(RegisterDto registerDto);
}
