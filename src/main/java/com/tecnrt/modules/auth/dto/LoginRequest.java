package com.tecnrt.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username/Roll Number or Email is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {}

