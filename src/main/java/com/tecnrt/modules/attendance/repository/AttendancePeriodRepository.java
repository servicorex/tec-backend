package com.tecnrt.modules.attendance.repository;

import com.tecnrt.modules.attendance.entity.AttendancePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendancePeriodRepository extends JpaRepository<AttendancePeriod, UUID> {
    List<AttendancePeriod> findByDepartmentIdOrderByCreatedAtDesc(String departmentId);
    Optional<AttendancePeriod> findByDepartmentIdAndYearAndSectionAndPeriodName(
            String departmentId, String year, String section, String periodName);
}

