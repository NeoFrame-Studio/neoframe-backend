package com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity;

import com.neoframe.neoframe_backend.core.domain.Plan;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Construtores obrigatórios do JPA
    public UserEntity() {}

    public UserEntity(UUID id, String email, String password, Plan plan, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.plan = plan;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
