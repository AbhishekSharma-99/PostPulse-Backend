package com.postpulse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 * <p>
 * Strategy:
 *  - We call doFilterInternal() directly via reflection helper — it's protected, so we
 *    test it by subclassing or by using the package-private trick. Because the class is
 *    in the same package as the test, we can call it directly after subclassing or by
 *    using MockMvc. Here we call it via a thin wrapper approach.
 *  - SecurityContextHolder is static state — MUST be cleared before each test to prevent
 *    cross-test contamination.
 *  - FilterChain.doFilter() must ALWAYS be called — even when auth fails — because
 *    stopping the chain breaks other filters (e.g., error handling).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    // We create a testable subclass because doFilterInternal is protected
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        // CRITICAL: always clear SecurityContext between tests
        SecurityContextHolder.clearContext();
    }

    // -----------------------------------------------------------------------
    // Happy path — valid token
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("doFilterInternal — sets authentication in SecurityContext for valid token")
    void doFilterInternal_whenValidToken_setsAuthenticationInContext() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String email = "user@postpulse.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    @DisplayName("doFilterInternal — filter chain always continues after valid token")
    void doFilterInternal_whenValidToken_filterChainProceeds() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        String email = "user@postpulse.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert — chain MUST continue
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // -----------------------------------------------------------------------
    // No token present
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("doFilterInternal — skips auth setup when Authorization header is absent")
    void doFilterInternal_whenNoAuthHeader_securityContextRemainsEmpty() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    @DisplayName("doFilterInternal — filter chain continues even when no token is present")
    void doFilterInternal_whenNoToken_filterChainStillProceeds() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // -----------------------------------------------------------------------
    // Malformed Bearer header
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("doFilterInternal — ignores header that does not start with 'Bearer '")
    void doFilterInternal_whenHeaderLacksBearerPrefix_skipsAuthentication() throws Exception {
        // Arrange — e.g., Basic auth header sent by mistake
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // -----------------------------------------------------------------------
    // Token fails validation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("doFilterInternal — does not set auth when token validation returns false")
    void doFilterInternal_whenTokenInvalid_securityContextRemainsEmpty() throws Exception {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal — does not call loadUserByUsername when token is invalid")
    void doFilterInternal_whenTokenInvalid_doesNotLoadUserDetails() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token.here");
        when(jwtTokenProvider.validateToken("bad.token.here")).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert — UserDetailsService should never be hit if token is invalid
        verifyNoInteractions(userDetailsService);
    }
}