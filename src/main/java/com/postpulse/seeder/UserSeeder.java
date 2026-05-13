package com.postpulse.seeder;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.repository.RoleRepository;
import com.postpulse.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@Slf4j
public class UserSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    List<User> seed() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new BlogAPIException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ROLE_ADMIN missing — check V1 migration"
                ));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BlogAPIException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ROLE_USER missing — check V1 migration"
                ));

        String encoded = passwordEncoder.encode("password@123");

        return userRepository.saveAll(
                List.of(
                    buildUser("Abhishek Sharma", "abhishek", "abhishek@postpulse.com", encoded, new HashSet<>(Set.of(adminRole, userRole))),
                    buildUser("Priya Mehta",     "priya",    "priya@postpulse.com",    encoded, new HashSet<>(Set.of(userRole))),
                    buildUser("Rohan Verma",     "rohan",    "rohan@postpulse.com",    encoded, new HashSet<>(Set.of(userRole)))
                )
        );
    }

    private User buildUser(String name, String username, String email,
                           String password, Set<Role> roles) {
        User u = new User();
        u.setName(name);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);
        u.setEnabled(true);
        u.setRoles(roles);
        return u;
    }
}