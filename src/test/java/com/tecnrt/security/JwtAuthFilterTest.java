package com.tecnrt.security;

import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(tokenProvider, userRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testTokenVersionMismatchRejectsAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        String rawToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + rawToken);
        when(tokenProvider.validateToken(rawToken)).thenReturn(true);
        when(tokenProvider.extractUserId(rawToken)).thenReturn(userId);
        when(tokenProvider.extractTokenVersion(rawToken)).thenReturn(1); // Old token version 1

        User userInDb = new User("21TML0501", "$2a$12$hashed", "STUDENT");
        userInDb.setId(userId);
        userInDb.setActive(true);
        userInDb.setTokenVersion(2); // DB token version incremented to 2 (after password change)

        when(userRepository.findById(userId)).thenReturn(Optional.of(userInDb));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Security Context must NOT be set because token version 1 != DB version 2
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testMatchingTokenVersionAuthenticatesSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String rawToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + rawToken);
        when(tokenProvider.validateToken(rawToken)).thenReturn(true);
        when(tokenProvider.extractUserId(rawToken)).thenReturn(userId);
        when(tokenProvider.extractTokenVersion(rawToken)).thenReturn(2);
        when(tokenProvider.extractDepartmentId(rawToken)).thenReturn(null);

        User userInDb = new User("21TML0501", "$2a$12$hashed", "STUDENT");
        userInDb.setId(userId);
        userInDb.setActive(true);
        userInDb.setTokenVersion(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userInDb));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userId, ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

