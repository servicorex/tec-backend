package com.tecnrt.modules.hod.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SubjectRequest(
        @NotBlank(message = "Subject code is required")
        String subjectCode,

        @NotBlank(message = "Subject name is required")
        String subjectName,

        @NotBlank(message = "Year is required")
        String year,

        @NotBlank(message = "Semester is required")
        String semester,

        String regulation, // 'R20', 'R23', 'R19', 'R16'
        String subjectType, // 'Theory', 'Laboratory', 'Elective'

        @NotNull(message = "Credits are required")
        @DecimalMin(value = "0.5", message = "Credits must be at least 0.5")
        BigDecimal credits
) {}

