package com.tecnrt.modules.student.repository;

import com.tecnrt.modules.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.profile WHERE s.user.id = :userId")
    Optional<Student> findByUserIdWithProfile(@Param("userId") UUID userId);

    Optional<Student> findByRollNumberIgnoreCase(String rollNumber);

    Optional<Student> findByUserId(UUID userId);

    List<Student> findByDepartmentIdOrderByRollNumberAsc(String departmentId);

    List<Student> findByDepartmentIdAndYearAndSectionOrderByRollNumberAsc(String departmentId, String year, String section);

    long countByDepartmentId(String departmentId);

    long countByDepartmentIdAndStatus(String departmentId, String status);

    boolean existsByRollNumberIgnoreCase(String rollNumber);
}

