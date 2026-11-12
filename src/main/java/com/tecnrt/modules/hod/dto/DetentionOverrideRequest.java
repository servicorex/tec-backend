package com.tecnrt.modules.hod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DetentionOverrideRequest(
        @NotNull(message = "Summary ID is required")
        UUID summaryId,

        @NotBlank(message = "Override reason is mandatory for statutory compliance")
        String reason
) {}

