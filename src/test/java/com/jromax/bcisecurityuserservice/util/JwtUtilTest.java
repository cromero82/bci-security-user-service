package com.jromax.bcisecurityuserservice.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    @DisplayName("generateToken should embed subject (email) and userId and not be expired")
    void generateToken_containsClaims_andNotExpired() {
        System.out.println("[DEBUG_LOG] START: generateToken_containsClaims_andNotExpired");
        JwtUtil jwtUtil = new JwtUtil();
        String email = "unit.test@example.com";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(email, userId);
        System.out.println("[DEBUG_LOG] Generated token length=" + (token == null ? 0 : token.length()));

        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");

        String subject = jwtUtil.extractUsername(token);
        UUID extractedUserId = jwtUtil.extractUserId(token);
        Date expiration = jwtUtil.extractExpiration(token);

        System.out.println("[DEBUG_LOG] Extracted subject=" + subject + ", userId=" + extractedUserId + ", expiration=" + expiration);

        assertEquals(email, subject, "Subject in token should match email");
        assertEquals(userId, extractedUserId, "Claim userId should match provided UUID");
        assertNotNull(expiration, "Expiration should not be null");
        assertTrue(expiration.after(Date.from(Instant.now())), "Token expiration should be in the future");

        // Expiration should be within 24h + small allowance (5 minutes) from now
        long millisUntilExp = expiration.getTime() - System.currentTimeMillis();
        long twentyFourHoursMillis = 24L * 60L * 60L * 1000L;
        long allowanceMillis = 5L * 60L * 1000L; // 5 minutes grace
        assertTrue(millisUntilExp <= (twentyFourHoursMillis + allowanceMillis), "Expiration should be within ~24h");
        System.out.println("[DEBUG_LOG] END: generateToken_containsClaims_andNotExpired");
    }

    @Test
    @DisplayName("validateToken should return true for same email and false for different email")
    void validateToken_matchesEmail() {
        System.out.println("[DEBUG_LOG] START: validateToken_matchesEmail");
        JwtUtil jwtUtil = new JwtUtil();
        String email = "jane.doe@example.com";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(email, userId);
        boolean isValidSame = jwtUtil.validateToken(token, email);
        boolean isValidDifferent = jwtUtil.validateToken(token, "other@example.com");

        System.out.println("[DEBUG_LOG] Validation results -> sameEmail=" + isValidSame + ", differentEmail=" + isValidDifferent);
        assertTrue(isValidSame, "Token should be valid for matching email");
        assertFalse(isValidDifferent, "Token should be invalid for non-matching email");
        System.out.println("[DEBUG_LOG] END: validateToken_matchesEmail");
    }

    @Test
    @DisplayName("tampered token should fail parsing/validation")
    void tamperedToken_shouldThrow() {
        System.out.println("[DEBUG_LOG] START: tamperedToken_shouldThrow");
        JwtUtil jwtUtil = new JwtUtil();
        String email = "john.doe@example.com";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(email, userId);
        // Tamper the token by changing the last character (simple corruption)
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        System.out.println("[DEBUG_LOG] Tampered token generated");

        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(tampered), "Parsing a tampered token should throw");
        assertThrows(JwtException.class, () -> jwtUtil.validateToken(tampered, email), "Validating a tampered token should throw");
        System.out.println("[DEBUG_LOG] END: tamperedToken_shouldThrow");
    }
}
