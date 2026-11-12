package com.tecnrt.modules.student.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentProfileUpdateRequest(
        // Personal Identification
        @Size(max = 100, message = "Short name cannot exceed 100 characters")
        String shortName,

        LocalDate dob,

        @Size(max = 10, message = "Gender cannot exceed 10 characters")
        String gender,

        @Size(max = 5, message = "Blood group cannot exceed 5 characters")
        String bloodGroup,

        @Pattern(regexp = "^$|^[+0-9\\s-]{10,20}$", message = "Invalid mobile number format")
        String studentMobile,

        // Contact & Residential
        @Pattern(regexp = "^$|^[+0-9\\s-]{10,20}$", message = "Invalid father's phone format")
        String fatherPhone,

        String fatherEmail,

        @Size(max = 500, message = "Current address cannot exceed 500 characters")
        String currentAddress,

        @Size(max = 500, message = "Permanent address cannot exceed 500 characters")
        String permanentAddress,

        // Parent / Guardian
        @Size(max = 255, message = "Father's name cannot exceed 255 characters")
        String fatherName,

        @Size(max = 255, message = "Father's occupation cannot exceed 255 characters")
        String fatherOccupation,

        @Size(max = 255, message = "Mother's name cannot exceed 255 characters")
        String motherName,

        @Size(max = 255, message = "Mother's occupation cannot exceed 255 characters")
        String motherOccupation,

        @Size(max = 255, message = "Guardian name cannot exceed 255 characters")
        String guardianName,

        @Pattern(regexp = "^$|^[+0-9\\s-]{10,20}$", message = "Invalid guardian contact format")
        String guardianContact
) {}

