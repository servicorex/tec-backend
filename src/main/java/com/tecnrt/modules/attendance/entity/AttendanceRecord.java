package com.tecnrt.modules.attendance.entity;

import com.tecnrt.modules.student.entity.Student;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_period_id", nullable = false)
    private AttendancePeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "classes_held", nullable = false)
    private int classesHeld;

    @Column(name = "classes_attended", nullable = false)
    private int classesAttended;

    @Column(name = "classes_absent", nullable = false)
    private int classesAbsent;

    @Column(name = "attendance_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendancePercentage;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = OffsetDateTime.now();
    }

    public AttendanceRecord() {}

    public AttendanceRecord(AttendancePeriod period, Student student, Subject subject, int classesHeld, int classesAttended, int classesAbsent, BigDecimal attendancePercentage) {
        this.period = period;
        this.student = student;
        this.subject = subject;
        this.classesHeld = classesHeld;
        this.classesAttended = classesAttended;
        this.classesAbsent = classesAbsent;
        this.attendancePercentage = attendancePercentage;
    }

    public UUID getId() { return id; }
    public AttendancePeriod getPeriod() { return period; }
    public Student getStudent() { return student; }
    public Subject getSubject() { return subject; }
    public int getClassesHeld() { return classesHeld; }
    public int getClassesAttended() { return classesAttended; }
    public int getClassesAbsent() { return classesAbsent; }
    public BigDecimal getAttendancePercentage() { return attendancePercentage; }

    public void setClassesHeld(int classesHeld) { this.classesHeld = classesHeld; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }
    public void setClassesAbsent(int classesAbsent) { this.classesAbsent = classesAbsent; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }
}

