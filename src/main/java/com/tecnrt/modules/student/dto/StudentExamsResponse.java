package com.tecnrt.modules.student.dto;

import java.util.List;

public record StudentExamsResponse(
        String currentSemester,
        String hallTicketNo,
        String centerName,
        boolean hallTicketReleased,
        List<ExamScheduleDto> regularSchedule,
        List<SupplementaryRecordDto> supplementaryRecords
) {
    public record ExamScheduleDto(
            String date,
            String timeSlot,
            String subjectCode,
            String subjectName,
            String assignedHall,
            String status
    ) {}

    public record SupplementaryRecordDto(
            String semester,
            String subjectCode,
            String subjectName,
            String status,
            String gradeObtained,
            int creditsEarned,
            String notificationDate
    ) {}
}

