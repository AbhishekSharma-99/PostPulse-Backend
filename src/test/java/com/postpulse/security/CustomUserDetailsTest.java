package com.postpulse.security;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CustomUserDetails.
 * <p>
 * Strategy:
 *  - Pure unit test — no Spring context, no DB, no IO.
 *  - We mock the User entity because the test owns only CustomUserDetails behavior,
 *    not User construction logic.
 *  - Validates the mapping contract: User fields → Spring Security UserDetails fields.
 */
@DisplayName("CustomUserDetails — Unit Tests")
class CustomUserDetailsTest {

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        User user = new User();
        user.setId(1L);
        user.setName("Abhishek Sharma");
        user.setUsername("abhishek_sharma");
        user.setEmail("abhishek@postpulse.com");
        user.setPassword("$2a$12$hashed_password");
        user.setEnabled(true);
        user.setRoles(Set.of(adminRole,userRole));

        userDetails = new CustomUserDetails(user);
    }

    // -----------------------------------------------------------------------
    // Identity mapping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getUserId — maps user entity ID correctly")
    void getUserId_mapsFromUserEntity() {
        assertThat(userDetails.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getName — maps user display name correctly")
    void getName_mapsFromUserEntity() {
        assertThat(userDetails.getName()).isEqualTo("Abhishek Sharma");
    }

    @Test
    @DisplayName("getCustomUsername — maps the username field, not email")
    void getCustomUsername_mapsUsernameNotEmail() {
        assertThat(userDetails.getCustomUsername()).isEqualTo("abhishek_sharma");
    }

    @Test
    @DisplayName("getEmail — maps email correctly")
    void getEmail_mapsFromUserEntity() {
        assertThat(userDetails.getEmail()).isEqualTo("abhishek@postpulse.com");
    }

    // -----------------------------------------------------------------------
    // Spring Security contract — getUsername() must return the login identity
    // -----------------------------------------------------------------------

    /**
     * CRITICAL: Spring Security's UserDetailsService.loadUserByUsername() identifies users
     * by the value returned from getUsername(). Since PostPulse logs in via email,
     * getUsername() MUST return email — not the display username.
     */
    @Test
    @DisplayName("getUsername (Spring Security) — returns EMAIL, not display username")
    void getUsername_returnsEmail_notDisplayUsername() {
        // This is the Spring Security contract method, not the custom getter
        assertThat(userDetails.getUsername()).isEqualTo("abhishek@postpulse.com");
    }

    // -----------------------------------------------------------------------
    // Password
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getPassword — returns BCrypt hashed password")
    void getPassword_returnsBCryptHashedPassword() {
        assertThat(userDetails.getPassword()).isEqualTo("$2a$12$hashed_password");
    }

    // -----------------------------------------------------------------------
    // Authorities (roles)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAuthorities — contains all roles from user entity")
    void getAuthorities_containsAllRoles() {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities).hasSize(2);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("getAuthorities — returns empty set when user has no roles")
    void getAuthorities_whenNoRoles_returnsEmptySet() {
        // Arrange
        User userWithNoRoles = new User();
        userWithNoRoles.setId(2L);
        userWithNoRoles.setName("No Role User");
        userWithNoRoles.setUsername("no_role_user");
        userWithNoRoles.setEmail("norole@postpulse.com");
        userWithNoRoles.setPassword("$2a$12$hashed_password");
        userWithNoRoles.setEnabled(true);
        userWithNoRoles.setRoles(Set.of()); // empty — default HashSet from entity also works

        CustomUserDetails noRoleDetails = new CustomUserDetails(userWithNoRoles);

        // Assert
        assertThat(noRoleDetails.getAuthorities()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Account status flags
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("isEnabled — returns true when user account is active")
    void isEnabled_whenUserIsActive_returnsTrue() {
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("isEnabled — returns false when user account is disabled")
    void isEnabled_whenUserIsDisabled_returnsFalse() {
        // Arrange
        User disabledUser = new User();
        disabledUser.setId(2L);
        disabledUser.setUsername("disabled_user");
        disabledUser.setEmail("disabled@postpulse.com");
        disabledUser.setPassword("$2a$12$hashed_password");
        disabledUser.setEnabled(false);

        CustomUserDetails disabledUserDetails = new CustomUserDetails(disabledUser);

        // Assert
        assertThat(disabledUserDetails.isEnabled()).isFalse();
    }
}