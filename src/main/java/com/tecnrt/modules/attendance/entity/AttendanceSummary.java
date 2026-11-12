package com.tecnrt.modules.attendance.entity;

import com.tecnrt.modules.student.entity.Student;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_summary")
public class AttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_period_id", nullable = false)
    private AttendancePeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "total_classes_held", nullable = false)
    private int totalClassesHeld;

    @Column(name = "total_classes_attended", nullable = false)
    private int totalClassesAttended;

    @Column(name = "total_classes_absent", nullable = false)
    private int totalClassesAbsent;

    @Column(name = "overall_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallPercentage;

    @Column(nullable = false, length = 30)
    private String status = "ELIGIBLE"; // 'ELIGIBLE', 'CONDONATION', 'DETAINED', 'ELIGIBLE_BY_OVERRIDE'

    @Column(name = "is_overridden", nullable = false)
    private boolean overridden = false;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private OffsetDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        this.calculatedAt = OffsetDateTime.now();
    }

    public AttendanceSummary() {}

    public AttendanceSummary(AttendancePeriod period, Student student, int totalClassesHeld, int totalClassesAttended, int totalClassesAbsent, BigDecimal overallPercentage, String status) {
        this.period = period;
        this.student = student;
        this.totalClassesHeld = totalClassesHeld;
        this.totalClassesAttended = totalClassesAttended;
        this.totalClassesAbsent = totalClassesAbsent;
        this.overallPercentage = overallPercentage;
        this.status = status;
        this.overridden = false;
    }

    public UUID getId() { return id; }
    public AttendancePeriod getPeriod() { return period; }
    public Student getStudent() { return student; }
    public int getTotalClassesHeld() { return totalClassesHeld; }
    public int getTotalClassesAttended() { return totalClassesAttended; }
    public int getTotalClassesAbsent() { return totalClassesAbsent; }
    public BigDecimal getOverallPercentage() { return overallPercentage; }
    public String getStatus() { return status; }
    public boolean isOverridden() { return overridden; }
    public String getOverrideReason() { return overrideReason; }

    public void setTotalClassesHeld(int totalClassesHeld) { this.totalClassesHeld = totalClassesHeld; }
    public void setTotalClassesAttended(int totalClassesAttended) { this.totalClassesAttended = totalClassesAttended; }
    public void setTotalClassesAbsent(int totalClassesAbsent) { this.totalClassesAbsent = totalClassesAbsent; }
    public void setOverallPercentage(BigDecimal overallPercentage) { this.overallPercentage = overallPercentage; }
    public void setStatus(String status) { this.status = status; }
    public void setOverridden(boolean overridden) { this.overridden = overridden; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}

