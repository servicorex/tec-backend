package com.tecnrt.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String SECRET = "4c65634e52542d53757065725365637265744b65792d323032362d5445432d4261636b656e64";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 12);
    }

    @Test
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "21TML0501", "STUDENT", 1, null);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(userId, tokenProvider.extractUserId(token));
        assertEquals("21TML0501", tokenProvider.extractUsername(token));
        assertEquals("STUDENT", tokenProvider.extractRole(token));
        assertEquals(1, tokenProvider.extractTokenVersion(token));
        assertNull(tokenProvider.extractDepartmentId(token));
    }

    @Test
    void testHodTokenIncludesDepartment() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "hod.cse@tecnrt.org", "HOD", 2, "CSE");

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("CSE", tokenProvider.extractDepartmentId(token));
        assertEquals("HOD", tokenProvider.extractRole(token));
        assertEquals(2, tokenProvider.extractTokenVersion(token));
    }

    @Test
    void testTamperedTokenRejection() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "21TML0501", "STUDENT", 1, null);

        // Tamper with payload
        String tampered = token.substring(0, token.length() - 5) + "ABCDE";
        assertFalse(tokenProvider.validateToken(tampered));
    }

    @Test
    void testExpiredTokenRejection() {
        // Provider with negative expiration for testing expiry
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1);
        UUID userId = UUID.randomUUID();
        String token = expiredProvider.generateToken(userId, "21TML0501", "STUDENT", 1, null);

        assertFalse(tokenProvider.validateToken(token));
    }
}

