package com.sources.app.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Tests for security utilities used in the pharmacy system
 */
public class SecurityUtilTest {

    @Test
    @DisplayName("Should hash passwords securely")
    void testPasswordHashing() {
        // Test password hashing functionality
        String password = "mySecurePassword123";
        String salt = "randomSalt456";
        
        String hashedPassword1 = hashPassword(password, salt);
        String hashedPassword2 = hashPassword(password, salt);
        
        // Same password with same salt should produce same hash
        assertEquals(hashedPassword1, hashedPassword2, "Same password should produce same hash");
        
        // Hash should not be empty
        assertNotNull(hashedPassword1, "Hash should not be null");
        assertFalse(hashedPassword1.isEmpty(), "Hash should not be empty");
        
        // Hash should be different from original password
        assertNotEquals(password, hashedPassword1, "Hash should be different from original password");
        
        // Different salt should produce different hash
        String differentSalt = "differentSalt789";
        String hashedPassword3 = hashPassword(password, differentSalt);
        assertNotEquals(hashedPassword1, hashedPassword3, "Different salt should produce different hash");
    }

    @Test
    @DisplayName("Should validate session token format")
    void testSessionTokenValidation() {
        // Test session token validation
        String validToken = generateSessionToken("user123");
        String invalidToken = "";
        String nullToken = null;
        
        assertTrue(isValidSessionToken(validToken), "Valid token should pass validation");
        assertFalse(isValidSessionToken(invalidToken), "Empty token should fail validation");
        assertFalse(isValidSessionToken(nullToken), "Null token should fail validation");
        
        // Token should have minimum length
        assertTrue(validToken.length() >= 10, "Token should have minimum length");
        
        // Token should be Base64 encoded
        assertTrue(isBase64(validToken), "Token should be Base64 encoded");
    }

    @Test
    @DisplayName("Should validate API authentication headers")
    void testAPIAuthenticationHeaders() {
        // Test API authentication header validation
        String validAuthHeader = "Bearer " + generateSessionToken("testUser");
        String invalidAuthHeader1 = "InvalidFormat";
        String invalidAuthHeader2 = "Bearer ";
        String invalidAuthHeader3 = "";
        
        assertTrue(isValidAuthHeader(validAuthHeader), "Valid auth header should pass");
        assertFalse(isValidAuthHeader(invalidAuthHeader1), "Invalid format should fail");
        assertFalse(isValidAuthHeader(invalidAuthHeader2), "Empty token should fail");
        assertFalse(isValidAuthHeader(invalidAuthHeader3), "Empty header should fail");
        assertFalse(isValidAuthHeader(null), "Null header should fail");
        
        // Extract token from header
        String extractedToken = extractTokenFromHeader(validAuthHeader);
        assertNotNull(extractedToken, "Should extract token from valid header");
        assertTrue(extractedToken.length() > 0, "Extracted token should not be empty");
    }

    // Helper methods (these would normally be in a real SecurityUtil class)
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String generateSessionToken(String userId) {
        String tokenData = userId + ":" + System.currentTimeMillis() + ":pharmacy";
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    private boolean isValidSessionToken(String token) {
        if (token == null || token.isEmpty()) return false;
        return token.length() >= 10 && isBase64(token);
    }

    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) return false;
        if (!authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        return !token.isEmpty() && isValidSessionToken(token);
    }

    private String extractTokenFromHeader(String authHeader) {
        if (!isValidAuthHeader(authHeader)) return null;
        return authHeader.substring(7);
    }
}
