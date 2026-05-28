package com.postpulse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postpulse.payload.auth.LoginRequest;
import com.postpulse.payload.auth.RegisterRequest;
import com.postpulse.payload.auth.RegisterResponse;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @ spins up ONLY the web layer (DispatcherServlet, filters, controllers).
 * No Spring Data, no real DB, no full ApplicationContext — fast and isolated.
 * <p>
 * SecurityFilterChain IS loaded here, so auth/CSRF behavior is real.
 * Use @WithMockUser where a security context is needed.
 * Use csrf() post-processor for mutating endpoints that go through CSRF filter.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    // ==================== LOGIN ====================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Valid credentials → 200 OK with JWT token and Bearer type")
        void login_ValidCredentials_Returns200WithToken() throws Exception {
            // Arrange — usernameOrEmail can be either a username or email
            LoginRequest request = new LoginRequest("user@example.com", "password123");
            String expectedToken = "eyJhbGciOiJIUzI1NiJ9.mocktoken";
            given(authService.login(any(LoginRequest.class))).willReturn(expectedToken);

            // Act & Assert — also verify tokenType defaults to "Bearer"
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(expectedToken))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Login with username (not email) → 200 OK")
        void login_WithUsername_Returns200() throws Exception {
            // Arrange — usernameOrEmail field accepts plain usernames too
            LoginRequest request = new LoginRequest("abhishek", "password123");
            given(authService.login(any(LoginRequest.class))).willReturn("token");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("token"));

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Alias /signin → 200 OK (dual-path mapping works)")
        void login_ViaSignInAlias_Returns200() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@example.com", "password123");
            given(authService.login(any(LoginRequest.class))).willReturn("token");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/signin").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Blank usernameOrEmail → 400 Bad Request, service not called")
        void login_BlankUsernameOrEmail_Returns400() throws Exception {
            // Arrange — @NotBlank on usernameOrEmail should reject empty string
            LoginRequest request = new LoginRequest("", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("Blank password → 400 Bad Request, service not called")
        void login_BlankPassword_Returns400() throws Exception {
            // Arrange — @NotBlank on password should reject empty string
            LoginRequest request = new LoginRequest("user@example.com", "");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }


        @Test
        @DisplayName("Malformed JSON body → 400 Bad Request")
        void login_MalformedJson_Returns400() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid " + "json }")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Empty request body → 400 Bad Request")
        void login_EmptyBody_Returns400() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== REGISTER ====================

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("Valid registration payload → 201 Created with RegisterResponse")
        void register_ValidPayload_Returns201() throws Exception {
            // Arrange
            RegisterRequest request = buildValidRegisterRequest();
            RegisterResponse response = new RegisterResponse(
                    1L,
                    "abhishek",
                    "abhishek@example.com",
                    "User registered successfully.");
            given(authService.register(any(RegisterRequest.class))).willReturn(response);

            // Act & Assert — verify all four RegisterResponse fields
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("abhishek"))
                    .andExpect(jsonPath("$.email").value("abhishek@example.com"))
                    .andExpect(jsonPath("$.message").value("User registered successfully."));

            verify(authService).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("Alias /signup → 201 Created (dual-path mapping works)")
        void register_ViaSignupAlias_Returns201() throws Exception {
            // Arrange
            RegisterRequest request = buildValidRegisterRequest();
            RegisterResponse response = new RegisterResponse(
                    1L,
                    "abhishek",
                    "abhishek@example.com",
                    "User registered successfully.");
            given(authService.register(any(RegisterRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Blank name → 400 Bad Request, service not called")
        void register_BlankName_Returns400() throws Exception {
            // Arrange — @NotBlank on name
            RegisterRequest request = new RegisterRequest("", "abhishek", "abhishek@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Blank username → 400 Bad Request, service not called")
        void register_BlankUsername_Returns400() throws Exception {
            // Arrange — @NotBlank on username
            RegisterRequest request = new RegisterRequest("Abhishek", "", "abhishek@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Username too short (< 3 chars) → 400 Bad Request, service not called")
        void register_UsernameTooShort_Returns400() throws Exception {
            // Arrange — @Size(min=3) on username; "ab" has only 2 characters
            RegisterRequest request = new RegisterRequest("Abhishek", "ab", "abhishek@example.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Username too long (> 20 chars) → 400 Bad Request, service not called")
        void register_UsernameTooLong_Returns400() throws Exception {
            // Arrange — @Size(max=20) on username; 21-character string exceeds limit
            RegisterRequest request = new RegisterRequest(
                    "Abhishek",
                    "a_username_that_is_way_too_long",
                    "abhishek@example.com",
                    "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Invalid email format → 400 Bad Request, service not called")
        void register_InvalidEmailFormat_Returns400() throws Exception {
            // Arrange — @Email rejects strings without proper email structure
            RegisterRequest request = new RegisterRequest("Abhishek", "abhishek", "not-an-email", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Blank email → 400 Bad Request, service not called")
        void register_BlankEmail_Returns400() throws Exception {
            // Arrange — @NotBlank on email
            RegisterRequest request = new RegisterRequest("Abhishek", "abhishek", "", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Password too short (< 8 chars) → 400 Bad Request, service not called")
        void register_PasswordTooShort_Returns400() throws Exception {
            // Arrange — @Size(min=8) on password; "pass1" has only 5 characters
            RegisterRequest request = new RegisterRequest("Abhishek", "abhishek", "abhishek@example.com", "pass1");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Blank password → 400 Bad Request, service not called")
        void register_BlankPassword_Returns400() throws Exception {
            // Arrange — @NotBlank on password
            RegisterRequest request = new RegisterRequest("Abhishek", "abhishek", "abhishek@example.com", "");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("Empty request body → 400 Bad Request")
        void register_EmptyBody_Returns400() throws Exception {
            // Act & Assert — all four @NotBlank fields missing
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== HELPERS ====================

    /**
     * Builds a fully valid RegisterRequest satisfying all constraints:
     * - name: @NotBlank
     * - username: @NotBlank + @Size(min=3, max=20)
     * - email: @NotBlank + @Email
     * - password: @NotBlank + @Size(min=8)
     */
    private RegisterRequest buildValidRegisterRequest() {
        return new RegisterRequest("Abhishek Sharma", "abhishek", "abhishek@example.com", "password123");
    }
}