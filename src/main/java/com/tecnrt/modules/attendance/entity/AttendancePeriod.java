package com.tecnrt.modules.attendance.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_periods")
public class AttendancePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(nullable = false, length = 20)
    private String year;

    @Column(nullable = false, length = 5)
    private String section;

    @Column(name = "period_name", nullable = false, length = 100)
    private String periodName;

    @Column(name = "period_type", length = 20)
    private String periodType = "monthly";

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT"; // 'DRAFT', 'PUBLISHED'

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public AttendancePeriod() {}

    public AttendancePeriod(String departmentId, String year, String section, String periodName, String periodType, String academicYear, String status, String fileName) {
        this.departmentId = departmentId;
        this.year = year;
        this.section = section;
        this.periodName = periodName;
        this.periodType = periodType;
        this.academicYear = academicYear;
        this.status = status;
        this.fileName = fileName;
    }

    public UUID getId() { return id; }
    public String getDepartmentId() { return departmentId; }
    public String getYear() { return year; }
    public String getSection() { return section; }
    public String getPeriodName() { return periodName; }
    public String getPeriodType() { return periodType; }
    public String getAcademicYear() { return academicYear; }
    public String getStatus() { return status; }
    public String getFileName() { return fileName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}

