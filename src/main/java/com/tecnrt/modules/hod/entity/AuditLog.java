package com.tecnrt.modules.hod.entity;

import com.tecnrt.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 20)
    private String role;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "affected_entity", length = 50)
    private String affectedEntity;

    @Column(name = "affected_id", length = 100)
    private String affectedId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public AuditLog() {}

    public AuditLog(User user, String role, String action, String details, String affectedEntity, String affectedId, String ipAddress) {
        this.user = user;
        this.role = role;
        this.action = action;
        this.details = details;
        this.affectedEntity = affectedEntity;
        this.affectedId = affectedId;
        this.ipAddress = ipAddress;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getRole() { return role; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public String getAffectedEntity() { return affectedEntity; }
    public String getAffectedId() { return affectedId; }
    public String getIpAddress() { return ipAddress; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

