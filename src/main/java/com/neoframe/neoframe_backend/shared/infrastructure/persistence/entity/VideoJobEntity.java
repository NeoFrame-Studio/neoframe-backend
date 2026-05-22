package com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_jobs")
public class VideoJobEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String script;

    @Column(nullable = false)
    private String status;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "background_music_url")
    private String backgroundMusicUrl;

    @Column(name = "intro_video_url")
    private String introVideoUrl;

    @Column(name = "topic_transition_url")
    private String topicTransitionUrl;

    // Construtor padrão para o JPA
    public VideoJobEntity() {}

    // Construtor completo para o Adaptador
    public VideoJobEntity(UUID id, UUID userId, String script, String status, String videoUrl,
                          LocalDateTime createdAt, LocalDateTime completedAt, String backgroundMusicUrl,
                          String introVideoUrl, String topicTransitionUrl) {
        this.id = id;
        this.userId = userId;
        this.script = script;
        this.status = status;
        this.videoUrl = videoUrl;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.backgroundMusicUrl = backgroundMusicUrl;
        this.introVideoUrl = introVideoUrl;
        this.topicTransitionUrl = topicTransitionUrl;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getBackgroundMusicUrl() { return backgroundMusicUrl; }
    public void setBackgroundMusicUrl(String backgroundMusicUrl) { this.backgroundMusicUrl = backgroundMusicUrl; }
    public String getIntroVideoUrl() { return introVideoUrl; }
    public void setIntroVideoUrl(String introVideoUrl) { this.introVideoUrl = introVideoUrl; }
    public String getTopicTransitionUrl() { return topicTransitionUrl; }
    public void setTopicTransitionUrl(String topicTransitionUrl) { this.topicTransitionUrl = topicTransitionUrl; }
}