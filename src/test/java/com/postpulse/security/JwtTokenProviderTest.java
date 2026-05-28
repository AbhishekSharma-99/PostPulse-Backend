package com.postpulse.security;

import com.postpulse.exception.BlogAPIException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtTokenProvider.
 * <p>
 * Strategy:
 *  - No Spring context (@SpringBootTest) — this class has zero Spring dependencies at test time.
 *  - We construct a real SecretKey so token generation/parsing are exercised against actual JJWT logic.
 *  - We craft malformed/expired tokens directly to drive every catch block in validateToken().
 */
@DisplayName("JwtTokenProvider — Unit Tests")
class JwtTokenProviderTest {

    // -----------------------------------------------------------------------
    // Constants & collaborators
    // -----------------------------------------------------------------------

    /**
     * 256-bit key — minimum for HMAC-SHA256.
     * Keys.secretKeyFor generates a secure random key; we encode it to Base64 so it
     * matches the format JwtTokenProvider expects from application properties.
     */
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            new byte[32] // 32-byte (256-bit) zero-filled key — deterministic for tests
    );
    private static final String JWT_SECRET_BASE64 =
            Encoders.BASE64.encode(SIGNING_KEY.getEncoded());

    private static final long EXPIRATION_MS        = 3_600_000L; // 1 hour
    private static final long ALREADY_EXPIRED_MS   = -1_000L;   // 1 second in the past

    private JwtTokenProvider jwtTokenProvider;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(JWT_SECRET_BASE64, EXPIRATION_MS);
    }

    // -----------------------------------------------------------------------
    // generateToken
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generateToken — returns non-blank JWT for authenticated principal")
    void generateToken_whenValidAuthentication_returnsNonBlankJwt() {
        // Arrange
        Authentication auth = buildAuthentication("user@postpulse.com");

        // Act
        String token = jwtTokenProvider.generateToken(auth);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("generateToken — subject claim equals authentication name")
    void generateToken_subjectClaimMatchesPrincipalName() {
        // Arrange
        String email = "admin@postpulse.com";
        Authentication auth = buildAuthentication(email);

        // Act
        String token = jwtTokenProvider.generateToken(auth);

        // Assert
        String extractedUsername = jwtTokenProvider.getUsername(token);
        assertThat(extractedUsername).isEqualTo(email);
    }

    // -----------------------------------------------------------------------
    // getUsername
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getUsername — extracts subject from a valid signed token")
    void getUsername_whenValidToken_returnsCorrectSubject() {
        // Arrange
        String expectedEmail = "writer@postpulse.com";
        String token = buildSignedToken(expectedEmail, EXPIRATION_MS);

        // Act
        String actualEmail = jwtTokenProvider.getUsername(token);

        // Assert
        assertThat(actualEmail).isEqualTo(expectedEmail);
    }

    // -----------------------------------------------------------------------
    // validateToken — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("validateToken — returns true for a fresh, correctly signed token")
    void validateToken_whenFreshValidToken_returnsTrue() {
        // Arrange
        String token = buildSignedToken("user@postpulse.com", EXPIRATION_MS);

        // Act
        boolean result = jwtTokenProvider.validateToken(token);

        // Assert
        assertThat(result).isTrue();
    }

    // -----------------------------------------------------------------------
    // validateToken — failure paths (one test per exception branch)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("validateToken — throws BlogAPIException for malformed (garbage) token")
    void validateToken_whenMalformedToken_throwsBlogAPIException() {
        // Arrange
        String malformedToken = "this.is.not.a.jwt";

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformedToken))
                .isInstanceOf(BlogAPIException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("validateToken — throws BlogAPIException for expired token")
    void validateToken_whenExpiredToken_throwsBlogAPIException() {
        // Arrange
        // Build a token whose expiration is set 1 second in the past
        String expiredToken = buildSignedToken("user@postpulse.com", ALREADY_EXPIRED_MS);

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(BlogAPIException.class)
                .hasMessageContaining("Expired JWT token");
    }

    @Test
    @DisplayName("validateToken — throws BlogAPIException for token with wrong signature")
    void validateToken_whenWrongSignature_throwsBlogAPIException() {
        // Arrange
        // Sign with a DIFFERENT key → JJWT throws SignatureException (subtype of MalformedJwtException)
        
        // Actually build with a distinctly different key value
        byte[] differentKeyBytes = new byte[32];
        differentKeyBytes[0] = 1; // differs by one byte
        SecretKey wrongKey = Keys.hmacShaKeyFor(differentKeyBytes);

        String tokenSignedWithWrongKey = Jwts.builder()
                .subject("user@postpulse.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(wrongKey)
                .compact();

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenSignedWithWrongKey))
                .isInstanceOf(BlogAPIException.class);
    }

    @Test
    @DisplayName("validateToken — throws BlogAPIException for blank token string")
    void validateToken_whenBlankToken_throwsBlogAPIException() {
        // Arrange
        String blankToken = "   ";

        // Act & Assert
        // IllegalArgumentException branch — blank string causes JJWT to throw it
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(blankToken))
                .isInstanceOf(BlogAPIException.class);
    }

    @Test
    @DisplayName("validateToken — throws BlogAPIException for unsigned (unsupported) JWT")
    void validateToken_whenUnsignedToken_throwsBlogAPIException() {
        // Arrange
        // Build a well-formed JWT with NO signature.
        // Jwts.builder() with no .signWith() produces an unsigned JWT (plain JWS).
        // parseSignedClaims() rejects this with UnsupportedJwtException because
        // it mandates a signed token — structure is valid, type is wrong.
        String unsignedToken = Jwts.builder()
                .subject("user@postpulse.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .compact(); // no .signWith() — produces unsigned JWT

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(unsignedToken))
                .isInstanceOf(BlogAPIException.class)
                .hasMessageContaining("Unsupported JWT token");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Builds a real signed JWT using the SAME key the JwtTokenProvider uses in tests.
     * This avoids coupling tests to generateToken() itself.
     */
    private String buildSignedToken(String subject, long expirationMs) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SIGNING_KEY)
                .compact();
    }

    private Authentication buildAuthentication(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        return auth;
    }
}