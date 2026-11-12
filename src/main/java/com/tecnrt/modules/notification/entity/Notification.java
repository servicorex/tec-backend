package com.tecnrt.modules.notification.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "department_id", length = 10)
    private String departmentId;

    @Column(name = "target_year", length = 20)
    private String targetYear;

    @Column(name = "target_section", length = 5)
    private String targetSection;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Notification() {}

    public Notification(String departmentId, String targetYear, String targetSection, String title, String message) {
        this.departmentId = departmentId;
        this.targetYear = targetYear;
        this.targetSection = targetSection;
        this.title = title;
        this.message = message;
    }

    public UUID getId() { return id; }
    public String getDepartmentId() { return departmentId; }
    public String getTargetYear() { return targetYear; }
    public String getTargetSection() { return targetSection; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

