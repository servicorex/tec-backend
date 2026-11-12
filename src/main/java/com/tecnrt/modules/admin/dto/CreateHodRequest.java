package com.tecnrt.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateHodRequest(
        @NotBlank(message = "Username/Email is required")
        @Email(message = "Must be a valid institutional email")
        String email,

        @NotBlank(message = "Department ID is required")
        String departmentId,

        @NotBlank(message = "Initial password is required")
        String initialPassword
) {}

