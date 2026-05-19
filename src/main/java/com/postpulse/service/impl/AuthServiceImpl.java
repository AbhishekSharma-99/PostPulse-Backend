package com.postpulse.service.impl;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.payload.auth.LoginRequest;
import com.postpulse.payload.auth.RegisterRequest;
import com.postpulse.payload.auth.RegisterResponse;
import com.postpulse.repository.RoleRepository;
import com.postpulse.repository.UserRepository;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String login(LoginRequest loginRequest) {
        log.debug("Login attempt for user/email: {}", loginRequest.getUsernameOrEmail());
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        log.info("User logged in successfully: {}", loginRequest.getUsernameOrEmail());
        return token;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BlogAPIException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Default role not found. Ensure database migrations have run."
                ));

        user.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {}", savedUser.getId());

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "User registered successfully."
        );
    }
}