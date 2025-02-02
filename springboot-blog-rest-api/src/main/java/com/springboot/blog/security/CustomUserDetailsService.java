package com.springboot.blog.security;

import com.springboot.blog.entity.User;
import com.springboot.blog.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

// Service class that implements UserDetailsService to load user-specific data
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Repository to interact with User data
    private UserRepository userRepository;

    // Constructor to inject UserRepository
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // Method to load user by username or email for authentication
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Fetch user from the database using either username or email
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User  not found with "
                        +"username or email : "+ usernameOrEmail));

        // Map user roles to GrantedAuthority for Spring Security
        Set<GrantedAuthority> authorities = user
                .getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // Convert role names to GrantedAuthority
                .collect(Collectors.toSet());

        // Return a UserDetails object containing user info and authorities
        return new org.springframework.security.core.userdetails.User(user.getEmail(), // Use email as username
                user.getPassword(), // User's password
                authorities); // User's authorities (roles)
    }
}