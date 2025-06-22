package com.interviewnotes.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for JwtUtils.
 * Tests JWT token generation, validation, and parsing.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtUtils jwtUtils;

    private static final String TEST_SECRET = "testSecretKeyForTestingPurposesOnly12345678901234567890testSecretKeyForTestingPurposesOnly12345678901234567890"; // 80 chars
    private static final int TEST_EXPIRATION = 3600000; // 1 hour
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_EXPIRATION);
    }

    @Test
    void testGenerateJwtToken_FromAuthentication() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        
        verify(authentication).getPrincipal();
        verify(userDetails).getUsername();
    }

    @Test
    void testGenerateTokenFromUsername() {
        // Act
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
        
        // Verify the token contains the username
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void testGenerateTokenFromUsername_WithEmptyUsername() {
        // Act
        String token = jwtUtils.generateTokenFromUsername("");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        assertNull(extractedUsername);
    }

    @Test
    void testGenerateTokenFromUsername_WithNullUsername() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.generateTokenFromUsername(null);
        });
        assertEquals("Username cannot be null", thrown.getMessage());
    }

    @Test
    void testGetUserNameFromJwtToken_ValidToken() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);

        // Act
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void testGetUserNameFromJwtToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtils.getUserNameFromJwtToken(invalidToken);
        });
    }

    @Test
    void testGetUserNameFromJwtToken_ExpiredToken() {
        // Arrange - Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1); // 1ms
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtils.getUserNameFromJwtToken(token);
        });
    }

    @Test
    void testGetUserNameFromJwtToken_NullToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.getUserNameFromJwtToken(null);
        });
    }

    @Test
    void testGetUserNameFromJwtToken_EmptyToken() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.getUserNameFromJwtToken("");
        });
    }

    @Test
    void testValidateJwtToken_ValidToken() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateJwtToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_ExpiredToken() {
        // Arrange - Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1); // 1ms
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_NullToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_EmptyToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_TokenWithWrongSignature() {
        // Arrange - Create token with different secret
        JwtUtils otherJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(otherJwtUtils, "jwtSecret", "differentSecretKey12345678901234567890differentSecretKey12345678901234567890"); // 64+ chars
        ReflectionTestUtils.setField(otherJwtUtils, "jwtExpirationMs", TEST_EXPIRATION);
        
        String token = otherJwtUtils.generateTokenFromUsername(TEST_USERNAME);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_MalformedToken() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateJwtToken_UnsupportedToken() {
        // Arrange - Create a token with unsupported algorithm
        String unsupportedToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYxNjE2MTYxNiwiZXhwIjoxNjE2MTY1MjE2fQ.";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(unsupportedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testTokenExpiration() {
        // Arrange - Create token with short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1000); // 1000ms
        String token = jwtUtils.generateTokenFromUsername(TEST_USERNAME);

        // Act - Validate immediately
        boolean isValidImmediately = jwtUtils.validateJwtToken(token);
        
        // Wait for expiration
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValidAfterExpiration = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValidImmediately);
        assertFalse(isValidAfterExpiration);
    }

    @Test
    void testTokenWithDifferentUsernames() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";

        // Act
        String token1 = jwtUtils.generateTokenFromUsername(username1);
        String token2 = jwtUtils.generateTokenFromUsername(username2);

        String extractedUsername1 = jwtUtils.getUserNameFromJwtToken(token1);
        String extractedUsername2 = jwtUtils.getUserNameFromJwtToken(token2);

        // Assert
        assertEquals(username1, extractedUsername1);
        assertEquals(username2, extractedUsername2);
        assertNotEquals(token1, token2);
    }

    @Test
    void testTokenWithSpecialCharacters() {
        // Arrange
        String usernameWithSpecialChars = "user@domain.com";

        // Act
        String token = jwtUtils.generateTokenFromUsername(usernameWithSpecialChars);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(usernameWithSpecialChars, extractedUsername);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testTokenWithLongUsername() {
        // Arrange
        String longUsername = "a".repeat(1000);

        // Act
        String token = jwtUtils.generateTokenFromUsername(longUsername);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(longUsername, extractedUsername);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testGenerateJwtToken_WithNullAuthentication() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtUtils.generateJwtToken(null);
        });
    }

    @Test
    void testGenerateJwtToken_WithNullPrincipal() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            jwtUtils.generateJwtToken(authentication);
        });
        
        verify(authentication).getPrincipal();
    }

    @Test
    void testValidateJwtToken_SignatureException() {
        // Tamper with a valid token to trigger SignatureException
        String token = jwtUtils.generateTokenFromUsername("testuser");
        String tampered = token.substring(0, token.length() - 2) + "ab";
        boolean result = jwtUtils.validateJwtToken(tampered);
        assertThat(result).isFalse();
    }

    @Test
    void testValidateJwtToken_WeakKeyException() {
        JwtUtils insecureUtils = new JwtUtils();
        ReflectionTestUtils.setField(insecureUtils, "jwtSecret", "short");
        ReflectionTestUtils.setField(insecureUtils, "jwtExpirationMs", 10000);
        boolean result = insecureUtils.validateJwtToken("any.token.value");
        assertThat(result).isFalse();
    }

    @Test
    void testValidateJwtToken_IllegalArgumentException_NullToken() {
        boolean result = jwtUtils.validateJwtToken(null);
        assertThat(result).isFalse();
    }

    @Test
    void testValidateJwtToken_IllegalArgumentException_EmptyToken() {
        boolean result = jwtUtils.validateJwtToken("");
        assertThat(result).isFalse();
    }
} 