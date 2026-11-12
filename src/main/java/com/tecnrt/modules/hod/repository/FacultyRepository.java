package com.tecnrt.modules.hod.repository;

import com.tecnrt.modules.hod.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    List<Faculty> findByDepartmentIdAndActiveTrueOrderByFullNameAsc(String departmentId);
    Optional<Faculty> findByStaffId(String staffId);
}

