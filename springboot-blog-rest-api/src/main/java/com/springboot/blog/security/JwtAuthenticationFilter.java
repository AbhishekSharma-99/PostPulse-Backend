package com.springboot.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter is a custom filter that extends OncePerRequestFilter.
 * It is responsible for processing JWT authentication tokens in incoming HTTP requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService userDetailsService;

    /**
     * Constructor to initialize JwtAuthenticationFilter with JwtTokenProvider and UserDetailsService.
     *
     * @param jwtTokenProvider the provider for JWT token operations
     * @param userDetailsService the service to load user-specific data
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters each request to check for a valid JWT token and sets the authentication in the security context.
     *
     * @param request the HttpServletRequest object containing the client's request
     * @param response the HttpServletResponse object for the response
     * @param filterChain the FilterChain for invoking the next filter
     * @throws ServletException if an error occurs during the filtering process
     * @throws IOException if an I/O error occurs during the filtering process
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Get JWT Token from HTTP request
        String token = getTokenFromRequest(request);

        // Validate token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Get username from the token
            String username = jwtTokenProvider.getUsername(token);

            // Load the user associated with the token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Create an authentication token
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // Set additional details for the authentication token
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the HTTP request's Authorization header.
     *
     * @param request the HttpServletRequest object containing the client's request
     * @return the JWT token as a String if present and valid, otherwise null
     */
    private String getTokenFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}