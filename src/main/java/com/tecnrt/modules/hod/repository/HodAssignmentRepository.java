package com.tecnrt.modules.hod.repository;

import com.tecnrt.modules.hod.entity.HodAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HodAssignmentRepository extends JpaRepository<HodAssignment, UUID> {
    Optional<HodAssignment> findByUserId(UUID userId);
    Optional<HodAssignment> findByDepartmentId(String departmentId);
}

