package com.neoframe.neoframe_backend.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private Plan plan;
    private LocalDateTime createdAt;

    // Novo campo para controlar os vídeos na fila/processando
    private int processingVideosCount = 0;

    public User(UUID id, String email, String passwordHash, Plan plan, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.plan = plan;
        this.createdAt = createdAt;
    }

    public boolean isAllowedToUploadCustomAssets() {
        return this.plan == Plan.PRO || this.plan == Plan.SAAS;
    }

    // --- Novos métodos para a regra de limite de processamento ---

    public int getProcessingVideosCount() {
        return processingVideosCount;
    }

    public void incrementProcessingCount() {
        this.processingVideosCount++;
    }

    public void decrementProcessingCount() {
        if (this.processingVideosCount > 0) {
            this.processingVideosCount--;
        }
    }

    public int getPlanLimit() {
        // Define o limite de concorrência com base no plano usando o switch do Java 21
        return switch (this.plan) {
            case STARTER -> 1;
            case PRO -> 3;
            case SAAS -> 10;
            default -> 1;
        };
    }

    // Getters originais
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Plan getPlan() { return plan; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void updatePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("A nova senha não pode ser vazia.");
        }
        this.passwordHash = newEncodedPassword;
    }
}