package com.tecnrt.modules.attendance.repository;

import com.tecnrt.modules.attendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByDepartmentIdAndSemesterAndActiveTrue(String departmentId, String semester);
    List<Subject> findByDepartmentIdAndSemesterAndRegulationAndActiveTrue(String departmentId, String semester, String regulation);
    List<Subject> findByDepartmentIdAndActiveTrueOrderBySemesterAscSubjectCodeAsc(String departmentId);
    Optional<Subject> findBySubjectCodeIgnoreCase(String subjectCode);
    Optional<Subject> findBySubjectCodeIgnoreCaseAndRegulationIgnoreCase(String subjectCode, String regulation);
    boolean existsBySubjectCodeIgnoreCaseAndRegulationIgnoreCase(String subjectCode, String regulation);
}

