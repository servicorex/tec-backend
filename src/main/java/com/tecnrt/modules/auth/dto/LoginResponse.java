package com.tecnrt.modules.auth.dto;

import java.util.UUID;

public record LoginResponse(
        boolean success,
        String token,
        String tokenType,
        long expiresInHours,
        UserInfo user
) {
    public record UserInfo(
            UUID id,
            String username,
            String role,
            String departmentId,
            boolean passwordChanged,
            boolean mustChangePassword
    ) {}

    public static LoginResponse of(String token, long expiresInHours, UUID id, String username, String role, String departmentId, boolean passwordChanged) {
        // If password has not yet been changed, mustChangePassword is true
        boolean mustChangePassword = !passwordChanged;
        return new LoginResponse(
                true,
                token,
                "Bearer",
                expiresInHours,
                new UserInfo(id, username, role, departmentId, passwordChanged, mustChangePassword)
        );
    }
}

