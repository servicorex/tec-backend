package com.tecnrt.modules.hod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(
        @NotBlank(message = "Roll number is required")
        @Size(max = 20)
        String rollNumber,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Year is required")
        String year,

        @NotBlank(message = "Semester is required")
        String semester,

        @NotBlank(message = "Section is required")
        String section,

        String admissionYear,
        String admissionType,
        boolean lateralEntry
) {}

