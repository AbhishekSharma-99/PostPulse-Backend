package com.postpulse.service.impl;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import com.postpulse.exception.BlogAPIException;
import com.postpulse.payload.LoginDto;
import com.postpulse.payload.RegisterDto;
import com.postpulse.payload.RegisterResponseDto;
import com.postpulse.repository.RoleRepository;
import com.postpulse.repository.UserRepository;
import com.postpulse.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginDto loginDto;
    private RegisterDto registerDto;
    private Role userRole;

    @BeforeEach
    void setUp() {
        loginDto = new LoginDto("abhishek@example.com", "password123");

        registerDto = new RegisterDto();
        registerDto.setName("Abhishek Sharma");
        registerDto.setUsername("abhishek");
        registerDto.setEmail("abhishek@example.com");
        registerDto.setPassword("password123");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
    }

    // =====================================================================
    // login()
    //
    // AuthenticationManager.authenticate() is the integration point with
    // Spring Security's full auth chain — we mock it here because the actual
    // chain (UserDetailsService → DB → BCrypt comparison) is tested in
    // integration tests, not unit tests. What we're verifying here:
    //   1. The correct token type is passed to authenticate()
    //   2. The authentication object returned is forwarded to generateToken()
    //   3. The token string returned by the provider is returned as-is
    //   4. Bad credentials propagate without being swallowed
    // =====================================================================

    @Test
    @DisplayName("Should return JWT token when credentials are valid")
    void login_Success() {

        // --- ARRANGE ---
        Authentication authentication = mock(Authentication.class);

        // authenticate() returns a fully-populated Authentication on success
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateToken(authentication))
                .thenReturn("mocked.jwt.token");

        // --- ACT ---
        String token = authService.login(loginDto);

        // --- ASSERT ---
        assertThat(token).isNotNull().isEqualTo("mocked.jwt.token");

        // Verify authenticate() received a token built from the DTO values —
        // ArgumentCaptor here confirms the principal and credentials were
        // correctly extracted from LoginDto and not hardcoded or swapped
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("abhishek@example.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");

        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when credentials are invalid")
    void login_InvalidCredentials_ThrowsBadCredentialsException() {

        // --- ARRANGE ---

        // AuthenticationManager throws BadCredentialsException when the
        // username doesn't exist or password doesn't match — the service
        // must NOT catch or wrap this; it should propagate as-is so
        // Spring Security's exception handling translates it to 401
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        // Token must never be generated when authentication fails
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    // =====================================================================
    // register()
    //
    // The service runs two existence checks before persisting, then
    // fetches ROLE_USER, builds a User, saves it, and maps the result.
    // Key decisions for each test:
    //
    //   - Happy path uses ArgumentCaptor on save() to assert the User
    //     was built correctly from RegisterDto — checking that password
    //     encoding was applied and role was assigned. any(User.class)
    //     would silently pass even if password was stored raw.
    //
    //   - Duplicate checks are tested independently so we can assert
    //     exactly which guard fired and that nothing downstream ran.
    //
    //   - ROLE_USER not found is an INTERNAL_SERVER_ERROR — it means
    //     Flyway didn't seed the roles table, which is a deployment
    //     failure, not a client error.
    // =====================================================================

    @Test
    @DisplayName("Should persist user and return RegisterResponseDto on successful registration")
    void register_Success() {

        // --- ARRANGE ---
        when(userRepository.existsByUsername("abhishek")).thenReturn(false);
        when(userRepository.existsByEmail("abhishek@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed_password");

        // savedUser simulates what the DB returns after INSERT — id is populated
        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setName("Abhishek Sharma");
        savedUser.setUsername("abhishek");
        savedUser.setEmail("abhishek@example.com");
        savedUser.setPassword("$2a$hashed_password");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // --- ACT ---
        RegisterResponseDto response = authService.register(registerDto);

        // --- ASSERT ---
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.username()).isEqualTo("abhishek");
        assertThat(response.email()).isEqualTo("abhishek@example.com");
        assertThat(response.message()).isEqualTo("User registered successfully.");

        // Capture the User passed to save() — this is the critical assertion:
        // confirms raw password was never persisted and role was correctly assigned.
        // any(User.class) above lets the save proceed; captor asserts the state.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getUsername()).isEqualTo("abhishek");
        assertThat(capturedUser.getEmail()).isEqualTo("abhishek@example.com");
        assertThat(capturedUser.getName()).isEqualTo("Abhishek Sharma");
        // BCrypt hash must be present — confirms encode() result was applied
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$hashed_password");
        // Role assignment — confirms ROLE_USER was set before save()
        assertThat(capturedUser.getRoles()).containsExactly(userRole);

        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("Should throw BlogAPIException(BAD_REQUEST) when username already exists")
    void register_UsernameAlreadyExists_ThrowsBlogAPIException() {

        // --- ARRANGE ---

        // existsByUsername returns true — first guard fires
        when(userRepository.existsByUsername("abhishek")).thenReturn(true);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(BlogAPIException.class)
                .hasMessage("Username already exists.")
                .satisfies(ex -> assertThat(((BlogAPIException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        // Second guard and everything downstream must never run
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw BlogAPIException(BAD_REQUEST) when email already exists")
    void register_EmailAlreadyExists_ThrowsBlogAPIException() {

        // --- ARRANGE ---

        // Username is unique but email is taken — second guard fires
        when(userRepository.existsByUsername("abhishek")).thenReturn(false);
        when(userRepository.existsByEmail("abhishek@example.com")).thenReturn(true);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(BlogAPIException.class)
                .hasMessage("Email already exists.")
                .satisfies(ex -> assertThat(((BlogAPIException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw BlogAPIException(INTERNAL_SERVER_ERROR) when ROLE_USER is not found in database")
    void register_DefaultRoleNotFound_ThrowsBlogAPIException() {

        // --- ARRANGE ---

        // Both uniqueness checks pass but ROLE_USER doesn't exist in roles table —
        // this means Flyway seeding failed or was skipped. Status must be
        // INTERNAL_SERVER_ERROR, NOT BAD_REQUEST — this is not a client error.
        when(userRepository.existsByUsername("abhishek")).thenReturn(false);
        when(userRepository.existsByEmail("abhishek@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed_password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(BlogAPIException.class)
                .hasMessage("Default role not found. Ensure database migrations have run.")
                .satisfies(ex -> assertThat(((BlogAPIException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));

        // User must never be persisted when role assignment fails
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should never store raw password — encoder must always be invoked before save")
    void register_PasswordIsAlwaysEncoded() {

        // --- ARRANGE ---

        // Isolated test for the encoding contract — separate from the happy path
        // so a refactor that moves or removes the encode() call is caught
        // independently, not buried inside a broader assertion chain.
        when(userRepository.existsByUsername("abhishek")).thenReturn(false);
        when(userRepository.existsByEmail("abhishek@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("abhishek");
        savedUser.setEmail("abhishek@example.com");
        savedUser.setPassword("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // --- ACT ---
        authService.register(registerDto);

        // --- ASSERT ---
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        // Raw password must never reach the repository
        assertThat(captor.getValue().getPassword())
                .isNotEqualTo("password123")
                .isEqualTo("$2a$encoded");

        verify(passwordEncoder).encode("password123");
    }
}