package com.tecnrt.modules.attendance.repository;

import com.tecnrt.modules.attendance.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, UUID> {

    @Query("SELECT s FROM AttendanceSummary s JOIN FETCH s.period " +
           "WHERE s.student.id = :studentId ORDER BY s.period.createdAt DESC")
    List<AttendanceSummary> findByStudentIdWithPeriod(@Param("studentId") UUID studentId);

    @Query("SELECT s FROM AttendanceSummary s JOIN FETCH s.period " +
           "WHERE s.student.id = :studentId AND s.period.status = 'PUBLISHED' " +
           "ORDER BY s.period.createdAt DESC LIMIT 1")
    Optional<AttendanceSummary> findLatestPublishedByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT s FROM AttendanceSummary s JOIN FETCH s.student st " +
           "WHERE s.period.id = :periodId ORDER BY st.rollNumber ASC")
    List<AttendanceSummary> findByPeriodIdWithStudent(@Param("periodId") UUID periodId);

    Optional<AttendanceSummary> findByPeriodIdAndStudentId(UUID periodId, UUID studentId);

    @Query("SELECT s FROM AttendanceSummary s WHERE s.period.departmentId = :deptId AND s.period.status = 'PUBLISHED'")
    List<AttendanceSummary> findByDepartmentId(@Param("deptId") String deptId);
}

