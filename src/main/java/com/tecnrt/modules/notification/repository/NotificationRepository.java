package com.tecnrt.modules.notification.repository;

import com.tecnrt.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE " +
           "(n.departmentId IS NULL OR n.departmentId = :deptId) AND " +
           "(n.targetYear IS NULL OR n.targetYear = 'ALL' OR n.targetYear = :year) AND " +
           "(n.targetSection IS NULL OR n.targetSection = 'ALL' OR n.targetSection = :section) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findForStudent(
            @Param("deptId") String deptId,
            @Param("year") String year,
            @Param("section") String section
    );
}

