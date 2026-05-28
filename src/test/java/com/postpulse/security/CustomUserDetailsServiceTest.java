package com.postpulse.security;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import com.postpulse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService.
 * <p>
 * Strategy:
 *  - @ExtendWith(MockitoExtension) — activates Mockito without Spring context.
 *  - We mock UserRepository because the test owns only CustomUserDetailsService behaviour.
 *  - We verify both the happy path and the contract-mandated UsernameNotFoundException.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService — Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");

        activeUser = new User();
        activeUser.setId(10L);
        activeUser.setName("Abhishek Sharma");
        activeUser.setUsername("abhishek_sharma");
        activeUser.setEmail("abhishek@postpulse.com");
        activeUser.setPassword("$2a$12$bcrypt_hash_here");
        activeUser.setEnabled(true);
        activeUser.setRoles(Set.of(role));
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("loadUserByUsername — returns CustomUserDetails when user found by email")
    void loadUserByUsername_whenUserFoundByEmail_returnsCustomUserDetails() {
        // Arrange
        String email = "abhishek@postpulse.com";
        when(userRepository.findByUsernameOrEmailWithRoles(email, email))
                .thenReturn(Optional.of(activeUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getUsername()).isEqualTo(email);
        verify(userRepository, times(1)).findByUsernameOrEmailWithRoles(email, email);
    }

    @Test
    @DisplayName("loadUserByUsername — returns CustomUserDetails when user found by username")
    void loadUserByUsername_whenUserFoundByUsername_returnsCustomUserDetails() {
        // Arrange
        String username = "abhishek_sharma";
        when(userRepository.findByUsernameOrEmailWithRoles(username, username))
                .thenReturn(Optional.of(activeUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isInstanceOf(CustomUserDetails.class);
        verify(userRepository, times(1)).findByUsernameOrEmailWithRoles(username, username);
    }

    @Test
    @DisplayName("loadUserByUsername — returned UserDetails has authorities populated")
    void loadUserByUsername_whenUserFound_authoritiesArePopulated() {
        // Arrange
        String email = "abhishek@postpulse.com";
        when(userRepository.findByUsernameOrEmailWithRoles(email, email))
                .thenReturn(Optional.of(activeUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }

    // -----------------------------------------------------------------------
    // Failure path — Spring Security contract
    // -----------------------------------------------------------------------

    /**
     * Spring Security's contract requires UsernameNotFoundException when the user is not found.
     * If you throw a different exception here, Spring Security catches it differently,
     * which can swallow authentication errors silently.
     */
    @Test
    @DisplayName("loadUserByUsername — throws UsernameNotFoundException when user not found")
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        // Arrange
        String nonExistentEmail = "ghost@postpulse.com";
        when(userRepository.findByUsernameOrEmailWithRoles(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(nonExistentEmail))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserByUsername — passes SAME value for both username and email params")
    void loadUserByUsername_passesInputAsBothUsernameAndEmailArguments() {
        // Arrange
        String input = "abhishek@postpulse.com";
        when(userRepository.findByUsernameOrEmailWithRoles(input, input))
                .thenReturn(Optional.of(activeUser));

        // Act
        userDetailsService.loadUserByUsername(input);

        // Assert — verify the dual-lookup contract explicitly
        verify(userRepository).findByUsernameOrEmailWithRoles(input, input);
        verifyNoMoreInteractions(userRepository);
    }
}