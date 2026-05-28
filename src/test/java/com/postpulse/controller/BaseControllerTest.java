package com.postpulse.controller;

import com.postpulse.config.SecurityConfig;
import com.postpulse.security.JwtAuthenticationEntryPoint;
import com.postpulse.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public abstract class BaseControllerTest {
    // This class is intentionally left blank. It serves as a common base for all controller tests,
    // providing shared configuration and setup (like importing security config) without duplicating code.
}
