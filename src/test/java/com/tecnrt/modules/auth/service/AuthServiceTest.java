package com.tecnrt.modules.auth.service;

import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.auth.dto.ChangePasswordRequest;
import com.tecnrt.modules.auth.dto.LoginRequest;
import com.tecnrt.modules.auth.dto.LoginResponse;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.entity.HodAssignment;
import com.tecnrt.modules.hod.repository.HodAssignmentRepository;
import com.tecnrt.security.JwtTokenProvider;
import com.tecnrt.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HodAssignmentRepository hodAssignmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, hodAssignmentRepository, passwordEncoder, tokenProvider, 12);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSuccessfulStudentLogin() {
        UUID userId = UUID.randomUUID();
        User studentUser = new User("21TML0501", "$2a$12$hashedPassword", "STUDENT");
        studentUser.setId(userId);
        studentUser.setActive(true);
        studentUser.setTokenVersion(1);

        when(userRepository.findByUsernameIgnoreCase("21TML0501")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("21TML0501", "$2a$12$hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(userId, "21TML0501", "STUDENT", 1, null)).thenReturn("mock.jwt.token");

        LoginResponse response = authService.login(new LoginRequest("  21tml0501  ", "21TML0501"));

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("mock.jwt.token", response.token());
        assertEquals("STUDENT", response.user().role());
        assertEquals("21TML0501", response.user().username());
    }

    @Test
    void testSuccessfulHodLoginWithDepartment() {
        UUID hodUserId = UUID.randomUUID();
        User hodUser = new User("hod.cse@tecnrt.org", "$2a$12$hashedHodPass", "HOD");
        hodUser.setId(hodUserId);
        hodUser.setActive(true);
        hodUser.setTokenVersion(1);

        HodAssignment assignment = new HodAssignment(hodUser, "CSE");

        when(userRepository.findByUsernameIgnoreCase("hod.cse@tecnrt.org")).thenReturn(Optional.of(hodUser));
        when(passwordEncoder.matches("SecureHod123", "$2a$12$hashedHodPass")).thenReturn(true);
        when(hodAssignmentRepository.findByUserId(hodUserId)).thenReturn(Optional.of(assignment));
        when(tokenProvider.generateToken(hodUserId, "hod.cse@tecnrt.org", "HOD", 1, "CSE")).thenReturn("hod.jwt.token");

        LoginResponse response = authService.login(new LoginRequest("hod.cse@tecnrt.org", "SecureHod123"));

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("HOD", response.user().role());
        assertEquals("CSE", response.user().departmentId());
    }

    @Test
    void testInvalidCredentialsThrowsGenericUnauthorized() {
        when(userRepository.findByUsernameIgnoreCase("UNKNOWN_USER")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                authService.login(new LoginRequest("UNKNOWN_USER", "wrongPassword")));

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testChangePasswordIncrementsTokenVersion() {
        UUID userId = UUID.randomUUID();
        User user = new User("21TML0501", "$2a$12$oldHashedPassword", "STUDENT");
        user.setId(userId);
        user.setTokenVersion(1);

        UserPrincipal principal = new UserPrincipal(userId, "21TML0501", "pass", "STUDENT", 1, true, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "$2a$12$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecretPassword")).thenReturn("$2a$12$newHashedPassword");

        authService.changePassword(new ChangePasswordRequest("oldPassword", "newSecretPassword"));

        // Verify token_version incremented to 2 for instant revocation of previous tokens
        assertEquals(2, user.getTokenVersion());
        assertTrue(user.isPasswordChanged());
        verify(userRepository, times(1)).save(user);
    }
}

