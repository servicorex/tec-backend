package com.tecnrt.modules.student.entity;

import com.tecnrt.modules.auth.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "roll_number", nullable = false, unique = true, length = 20)
    private String rollNumber;

    @Column(name = "admission_number", unique = true, length = 30)
    private String admissionNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(nullable = false, length = 20)
    private String year; // '1st Year', '2nd Year', '3rd Year', '4th Year'

    @Column(nullable = false, length = 30)
    private String semester; // 'Sem 1-1', 'Sem 1-2', etc.

    @Column(nullable = false, length = 5)
    private String section; // 'A', 'B', 'C'

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(nullable = false, length = 50)
    private String course = "B.Tech";

    @Column(name = "admission_year", length = 10)
    private String admissionYear;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "admission_type", length = 50)
    private String admissionType;

    @Column(name = "admission_quota", length = 50)
    private String admissionQuota;

    @Column(name = "caste_category", length = 20)
    private String casteCategory;

    @Column(name = "is_lateral_entry", nullable = false)
    private boolean lateralEntry = false;

    @Column(nullable = false, length = 20)
    private String status = "Active";

    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus = "PENDING";

    @Column(name = "profile_status", nullable = false, length = 20)
    private String profileStatus = "Incomplete";

    @Column(name = "profile_completion", nullable = false)
    private int profileCompletion = 0;

    @Column(precision = 3, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private StudentProfile profile;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Student() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(String admissionYear) { this.admissionYear = admissionYear; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public String getAdmissionType() { return admissionType; }
    public void setAdmissionType(String admissionType) { this.admissionType = admissionType; }

    public String getAdmissionQuota() { return admissionQuota; }
    public void setAdmissionQuota(String admissionQuota) { this.admissionQuota = admissionQuota; }

    public String getCasteCategory() { return casteCategory; }
    public void setCasteCategory(String casteCategory) { this.casteCategory = casteCategory; }

    public boolean isLateralEntry() { return lateralEntry; }
    public void setLateralEntry(boolean lateralEntry) { this.lateralEntry = lateralEntry; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getProfileStatus() { return profileStatus; }
    public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }

    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }

    public StudentProfile getProfile() { return profile; }
    public void setProfile(StudentProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setStudent(this);
        }
    }
}

