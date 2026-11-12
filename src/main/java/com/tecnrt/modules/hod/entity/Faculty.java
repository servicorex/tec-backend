package com.tecnrt.modules.hod.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "staff_id", nullable = false, unique = true, length = 30)
    private String staffId;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(length = 100)
    private String designation;

    @Column
    private String qualification;

    @Column(length = 50)
    private String experience;

    @Column
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "workload_hours")
    private Integer workloadHours = 16;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Faculty() {}

    public Faculty(String staffId, String departmentId, String fullName, String designation, String qualification, String experience, String email, String phone) {
        this.staffId = staffId;
        this.departmentId = departmentId;
        this.fullName = fullName;
        this.designation = designation;
        this.qualification = qualification;
        this.experience = experience;
        this.email = email;
        this.phone = phone;
        this.active = true;
    }

    public UUID getId() { return id; }
    public String getStaffId() { return staffId; }
    public String getDepartmentId() { return departmentId; }
    public String getFullName() { return fullName; }
    public String getDesignation() { return designation; }
    public String getQualification() { return qualification; }
    public String getExperience() { return experience; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Integer getWorkloadHours() { return workloadHours; }
    public boolean isActive() { return active; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setWorkloadHours(Integer workloadHours) { this.workloadHours = workloadHours; }
    public void setActive(boolean active) { this.active = active; }
}

