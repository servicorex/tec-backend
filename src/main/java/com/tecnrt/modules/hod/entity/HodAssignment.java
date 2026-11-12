package com.tecnrt.modules.hod.entity;

import com.tecnrt.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hod_assignments")
public class HodAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "department_id", nullable = false, length = 10)
    private String departmentId;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = true;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = OffsetDateTime.now();
    }

    public HodAssignment() {}

    public HodAssignment(User user, String departmentId) {
        this.user = user;
        this.departmentId = departmentId;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getDepartmentId() { return departmentId; }
}

