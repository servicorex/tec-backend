package com.tecnrt.modules.hod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSectionRequest(
        @NotBlank(message = "Section name is required")
        @Size(max = 5, message = "Section name cannot exceed 5 characters")
        String sectionName,

        String academicYear
) {}

