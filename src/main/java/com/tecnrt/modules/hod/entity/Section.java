package com.tecnrt.modules.hod.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(name = "section_name", nullable = false, length = 5)
    private String sectionName;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Section() {}

    public Section(String departmentId, String sectionName, String academicYear) {
        this.departmentId = departmentId;
        this.sectionName = sectionName;
        this.academicYear = academicYear;
        this.active = true;
    }

    public UUID getId() { return id; }
    public String getDepartmentId() { return departmentId; }
    public String getSectionName() { return sectionName; }
    public String getAcademicYear() { return academicYear; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}

