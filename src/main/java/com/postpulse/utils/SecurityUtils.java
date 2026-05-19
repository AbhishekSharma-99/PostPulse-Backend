package com.postpulse.utils;

import com.postpulse.entity.User;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.exception.ResourceNotFoundException;
import com.postpulse.repository.UserRepository;
import com.postpulse.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extracts the authenticated user's ID from the security context.
     *
     * @return the user ID of the currently authenticated user
     * @throws BlogAPIException if no authenticated user found or principal is not CustomUserDetails
     */
    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BlogAPIException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated or invalid principal type"
            );
        }
        return userDetails.getUserId();
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public Collection<GrantedAuthority> getCurrentUserAuthorities() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BlogAPIException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated or invalid principal type"
            );
        }

        // getAuthorities() returns Collection<? extends GrantedAuthority>
        // Cast is safe — CustomUserDetails.authorities is Set<GrantedAuthority>
        // (built via SimpleGrantedAuthority which directly implements GrantedAuthority)
        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();
        return authorities;
    }
}