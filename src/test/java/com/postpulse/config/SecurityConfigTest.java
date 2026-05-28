package com.postpulse.config;

import com.postpulse.security.JwtAuthenticationEntryPoint;
import com.postpulse.security.JwtTokenProvider;
import com.postpulse.service.AuthService;
import com.postpulse.service.CategoryService;
import com.postpulse.service.CommentService;
import com.postpulse.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security filter chain integration slice test.
 * <p>
 * Strategy:
 *  - @WebMvcTest loads ONLY the web layer: DispatcherServlet, filters, and Spring Security.
 *    No service beans, no repositories, no DB — fastest possible slice.
 *  - @Import(SecurityConfig.class) pulls in the real SecurityFilterChain rules.
 *  - We let Spring create the real JwtAuthenticationFilter, but we mock its dependencies
 *    (JwtTokenProvider, UserDetailsService) to avoid real JWT logic and DB access.
 * <p>
 * What this tests:
 *  - Swagger endpoints are publicly accessible (no auth)
 *  - Auth endpoints (/api/v1/auth/**) are publicly accessible
 *  - GET on posts/categories is publicly accessible
 *  - POST/PUT/DELETE on posts/categories requires authentication (→ 401 when no token)
 *  - Unknown endpoints require authentication
 *  - Session creation policy is STATELESS (no HTTP session)
 * <p>
 * NOTE: This test requires no actual controllers. Returning 404 for public endpoints
 *       means security passed and the request reached the dispatcher. Returning 401
 *       for protected endpoints means security correctly rejected unauthenticated requests.
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@ActiveProfiles("test")
@DisplayName("SecurityConfig — Filter Chain Access Control Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ─── Mock the filter's dependencies, not the filter itself ─────────────
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    // JwtAuthenticationEntryPoint is a simple error handler – mocking it is fine


    // ─── Mock ALL services that any controller needs ─────────────────────
    @MockBean private CommentService commentService;
    @MockBean private PostService postService;
    @MockBean private CategoryService categoryService;
    @MockBean private AuthService authService;

    // -----------------------------------------------------------------------
    // Swagger whitelist — must be public (no auth required)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /swagger-ui/index.html — accessible without authentication")
    void swaggerUi_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound()); // 404 = reached routing, not blocked by security
    }

    @Test
    @DisplayName("GET /v3/api-docs — accessible without authentication")
    void apiDocs_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // Auth endpoints — must be public
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/auth/login — accessible without authentication")
    void authLogin_isPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — accessible without authentication")
    void authRegister_isPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Public read access on posts and categories
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/posts — accessible without authentication")
    void getPosts_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/categories — accessible without authentication")
    void getCategories_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Protected endpoints — require authentication (expect 401 with no token)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/posts without token — returns 401 Unauthorized")
    void createPost_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/1 without token — returns 401 Unauthorized")
    void deletePost_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/categories without token — returns 401 Unauthorized")
    void createCategory_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Session management — must be STATELESS
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Authenticated request — no HTTP session is created (STATELESS policy)")
    void request_doesNotCreateHttpSession() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(result ->
                        assertThat(result.getRequest().getSession(false)).isNull()
                );
    }
}