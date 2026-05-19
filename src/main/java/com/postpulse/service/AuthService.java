package com.postpulse.service;

import com.postpulse.payload.auth.LoginRequest;
import com.postpulse.payload.auth.RegisterRequest;
import com.postpulse.payload.auth.RegisterResponse;

public interface AuthService {
    String login(LoginRequest loginRequest);

    RegisterResponse register(RegisterRequest registerRequest);
}
