package com.tecnrt.modules.hod.repository;

import com.tecnrt.modules.hod.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByDepartmentIdAndActiveTrueOrderBySectionNameAsc(String departmentId);
    Optional<Section> findByDepartmentIdAndSectionNameAndAcademicYear(String departmentId, String sectionName, String academicYear);
    boolean existsByDepartmentIdAndSectionName(String departmentId, String sectionName);
}

