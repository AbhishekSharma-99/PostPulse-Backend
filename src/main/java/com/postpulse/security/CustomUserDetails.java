package com.postpulse.security;

import com.postpulse.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long userId;
    @Getter
    private final String name;
    @Getter
    private final String customUsername;
    @Getter
    private final String email;

    private final Boolean enabled;

    private final String password;

    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.customUsername = user.getUsername();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.password = user.getPassword();
        this.authorities = user.getRoles()
                .stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
