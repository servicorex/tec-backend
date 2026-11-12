package com.tecnrt.modules.hod.dto;

import java.util.UUID;

public record FacultyResponse(
        UUID id,
        String staffId,
        String departmentId,
        String fullName,
        String designation,
        String qualification,
        String experience,
        String email,
        String phone,
        Integer workloadHours,
        boolean active
) {}

