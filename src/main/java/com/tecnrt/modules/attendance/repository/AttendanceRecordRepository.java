package com.tecnrt.modules.attendance.repository;

import com.tecnrt.modules.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    @Query("SELECT r FROM AttendanceRecord r JOIN FETCH r.subject JOIN FETCH r.period " +
           "WHERE r.student.id = :studentId ORDER BY r.period.createdAt DESC")
    List<AttendanceRecord> findByStudentIdWithDetails(@Param("studentId") UUID studentId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.period.id = :periodId AND r.student.id = :studentId AND r.subject.id = :subjectId")
    java.util.Optional<AttendanceRecord> findByPeriodIdAndStudentIdAndSubjectId(
            @Param("periodId") UUID periodId,
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId
    );
}
