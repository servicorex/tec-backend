package com.tecnrt.modules.attendance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "subject_code", nullable = false, unique = true, length = 20)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(nullable = false, length = 20)
    private String year;

    @Column(nullable = false, length = 30)
    private String semester;

    @Column(nullable = false, length = 20)
    private String regulation = "R20"; // 'R20', 'R23', 'R19', 'R16'

    @Column(name = "subject_type", nullable = false, length = 50)
    private String subjectType = "Theory"; // 'Theory', 'Laboratory', 'Project', 'Elective'

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal credits = BigDecimal.valueOf(3.0);

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Subject() {}

    public Subject(String subjectCode, String subjectName, String departmentId, String year, String semester, BigDecimal credits) {
        this(subjectCode, subjectName, departmentId, year, semester, "R20", "Theory", credits);
    }

    public Subject(String subjectCode, String subjectName, String departmentId, String year, String semester, String regulation, String subjectType, BigDecimal credits) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.departmentId = departmentId;
        this.year = year;
        this.semester = semester;
        this.regulation = regulation != null ? regulation : "R20";
        this.subjectType = subjectType != null ? subjectType : "Theory";
        this.credits = credits != null ? credits : BigDecimal.valueOf(3.0);
    }

    public UUID getId() { return id; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public String getDepartmentId() { return departmentId; }
    public String getYear() { return year; }
    public String getSemester() { return semester; }
    public String getRegulation() { return regulation; }
    public String getSubjectType() { return subjectType; }
    public BigDecimal getCredits() { return credits; }
    public boolean isActive() { return active; }

    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setYear(String year) { this.year = year; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setRegulation(String regulation) { this.regulation = regulation; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public void setCredits(BigDecimal credits) { this.credits = credits; }
    public void setActive(boolean active) { this.active = active; }
}

