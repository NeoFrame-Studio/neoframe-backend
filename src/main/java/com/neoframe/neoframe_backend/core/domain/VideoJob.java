package com.neoframe.neoframe_backend.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class VideoJob {
    private UUID id;
    private UUID userId;
    private String script;
    private VideoStatus status;
    private String videoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Optional fields for manual file uploads (Starter Plan requirements)
    private String backgroundMusicUrl;
    private String introVideoUrl;
    private String topicTransitionUrl;

    public VideoJob(UUID id, UUID userId, String script, VideoStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.script = script;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Business behavior to transition status safely
    public void startProcessing() {
        if (this.status != VideoStatus.PENDING) {
            throw new IllegalStateException("Job can only start processing if it is PENDING");
        }
        this.status = VideoStatus.PROCESSING;
    }

    public void complete(String finalVideoUrl) {
        this.status = VideoStatus.COMPLETED;
        this.videoUrl = finalVideoUrl;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = VideoStatus.FAILED;
    }

    // Getters and Setters for optional assets
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getScript() { return script; }
    public VideoStatus getStatus() { return status; }
    public String getVideoUrl() { return videoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public String getBackgroundMusicUrl() { return backgroundMusicUrl; }
    public void setBackgroundMusicUrl(String backgroundMusicUrl) { this.backgroundMusicUrl = backgroundMusicUrl; }

    public String getIntroVideoUrl() { return introVideoUrl; }
    public void setIntroVideoUrl(String introVideoUrl) { this.introVideoUrl = introVideoUrl; }

    public String getTopicTransitionUrl() { return topicTransitionUrl; }
    public void setTopicTransitionUrl(String topicTransitionUrl) { this.topicTransitionUrl = topicTransitionUrl; }

    public void validateRequirements(Plan userPlan) {
        if (this.script == null || this.script.trim().isEmpty()) {
            throw new IllegalArgumentException("Script cannot be empty.");
        }

        // Enforce Starter plan constraints
        if (userPlan == Plan.STARTER) {
            boolean isMissingManualAssets =
                    this.backgroundMusicUrl == null || this.backgroundMusicUrl.trim().isEmpty() ||
                            this.introVideoUrl == null || this.introVideoUrl.trim().isEmpty() ||
                            this.topicTransitionUrl == null || this.topicTransitionUrl.trim().isEmpty();

            if (isMissingManualAssets) {
                throw new IllegalStateException(
                        "Starter plan requires manual upload of background music, intro video, and transition video."
                );
            }
        }
    }
}
