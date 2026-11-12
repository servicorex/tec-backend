package com.tecnrt.modules.auth.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.auth.dto.ChangePasswordRequest;
import com.tecnrt.modules.auth.dto.LoginRequest;
import com.tecnrt.modules.auth.dto.LoginResponse;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.entity.HodAssignment;
import com.tecnrt.modules.hod.repository.HodAssignmentRepository;
import com.tecnrt.security.JwtTokenProvider;
import com.tecnrt.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final HodAssignmentRepository hodAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final long expirationHours;

    // Simple in-memory rate-limiter for basic abuse protection
    private final Map<String, LoginAttemptTracker> attemptsCache = new ConcurrentHashMap<>();

    private static class LoginAttemptTracker {
        int attempts = 0;
        long lockUntil = 0;
    }

    public AuthService(
            UserRepository userRepository,
            HodAssignmentRepository hodAssignmentRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            @Value("${app.jwt.expiration-hours:12}") long expirationHours) {
        this.userRepository = userRepository;
        this.hodAssignmentRepository = hodAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.expirationHours = expirationHours;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedUsername = request.username().trim();

        // Check abuse throttle
        checkThrottle(normalizedUsername);

        // Normalize: if looks like a student roll number (no @), uppercase it
        if (!normalizedUsername.contains("@")) {
            normalizedUsername = normalizedUsername.toUpperCase();
        }

        User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElse(null);

        // Constant-time-like generic error to avoid user enumeration
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordFailedAttempt(normalizedUsername);
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account has been deactivated. Please contact administration.");
        }

        // Reset failed attempts on success
        attemptsCache.remove(normalizedUsername.toLowerCase());

        String departmentId = null;
        if ("HOD".equalsIgnoreCase(user.getRole())) {
            departmentId = hodAssignmentRepository.findByUserId(user.getId())
                    .map(HodAssignment::getDepartmentId)
                    .orElse(null);
        }

        String token = tokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getTokenVersion(),
                departmentId
        );

        return LoginResponse.of(
                token,
                expirationHours,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                departmentId,
                user.isPasswordChanged()
        );
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Set new password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChanged(true);

        // CRITICAL: Increment token version to immediately revoke all existing JWTs
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    private void checkThrottle(String key) {
        LoginAttemptTracker tracker = attemptsCache.get(key.toLowerCase());
        if (tracker != null && tracker.lockUntil > System.currentTimeMillis()) {
            throw new UnauthorizedException("Too many failed attempts. Account temporarily locked for 15 minutes.");
        }
    }

    private void recordFailedAttempt(String key) {
        LoginAttemptTracker tracker = attemptsCache.computeIfAbsent(key.toLowerCase(), k -> new LoginAttemptTracker());
        tracker.attempts++;
        if (tracker.attempts >= 5) {
            tracker.lockUntil = System.currentTimeMillis() + (15 * 60 * 1000); // 15 mins
            tracker.attempts = 0;
        }
    }
}

