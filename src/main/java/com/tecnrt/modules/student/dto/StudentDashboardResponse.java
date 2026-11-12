package com.tecnrt.modules.student.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentDashboardResponse(
        String name,
        String rollNumber,
        String department,
        String year,
        String semester,
        String section,
        BigDecimal attendanceOverall,
        int totalClassesHeld,
        int totalClassesAttended,
        int totalClassesAbsent,
        String attendanceStatus, // 'ELIGIBLE', 'CONDONATION', 'DETAINED'
        BigDecimal cgpa,
        BigDecimal lastSemSgpa,
        String academicStanding,
        int enrolledCoursesCount,
        BigDecimal totalCredits,
        List<EnrolledSubjectDto> enrolledSubjects,
        ExamNoticeDto examNotice
) {
    public record EnrolledSubjectDto(
            String code,
            String name,
            String faculty,
            BigDecimal credits,
            int classesHeld,
            int classesAttended,
            BigDecimal attendancePct,
            String syllabusCompleted
    ) {}

    public record ExamNoticeDto(
            String currentSemester,
            String status,
            String hallTicketNo,
            String centerName,
            boolean hallTicketReleased
    ) {}
}

