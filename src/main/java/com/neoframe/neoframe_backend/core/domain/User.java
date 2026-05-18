package com.neoframe.neoframe_backend.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private Plan plan;
    private LocalDateTime createdAt;

    public User(UUID id, String email, String passwordHash, Plan plan, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.plan = plan;
        this.createdAt = createdAt;
    }

    // Business rule validation example
    public boolean isAllowedToUploadCustomAssets() {
        // Only PRO and SAAS users can bypass the starter automated constraints if needed
        return this.plan == Plan.PRO || this.plan == Plan.SAAS;
    }

    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Plan getPlan() { return plan; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
