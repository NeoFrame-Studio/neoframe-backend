package com.neoframe.neoframe_backend.modules.video.core.domain;

import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;

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

    private String backgroundMusicUrl;
    private String introVideoUrl;
    private String topicTransitionUrl;

    // 1. CONSTRUTOR PARA O SERVICE (Cria novos Jobs e Valida Regras)
    public VideoJob(UUID id, UUID userId, String script, VideoStatus status, LocalDateTime createdAt,
                    String backgroundMusicUrl, String introVideoUrl, String topicTransitionUrl, Plan userPlan) {
        this.id = id;
        this.userId = userId;
        this.script = script;
        this.status = status;
        this.createdAt = createdAt;
        this.backgroundMusicUrl = backgroundMusicUrl;
        this.introVideoUrl = introVideoUrl;
        this.topicTransitionUrl = topicTransitionUrl;

        // Valida no momento do nascimento!
        validateRequirements(userPlan);
    }

    // 2. CONSTRUTOR PARA O ADAPTER/BANCO DE DADOS (Hidrata o objeto sem revalidar regras)
    public VideoJob(UUID id, UUID userId, String script, VideoStatus status, String videoUrl,
                    LocalDateTime createdAt, LocalDateTime completedAt,
                    String backgroundMusicUrl, String introVideoUrl, String topicTransitionUrl) {
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

    public void startProcessing() {
        if (this.status != VideoStatus.PENDING) {
            throw new IllegalStateException("Job can only start processing if it is PENDING");
        }
        this.status = VideoStatus.PROCESSING;
    }

    public void finalizeCuration() {
        if (this.status != VideoStatus.WAITING_CURATION) {
            throw new IllegalStateException("Job can only be finalized if it is WAITING_CURATION");
        }
        this.status = VideoStatus.PROCESSING;
    }

    public void readyForCuration(String jsonUrl) {
        this.status = VideoStatus.WAITING_CURATION;
        this.videoUrl = jsonUrl;
    }

    public void complete(String finalVideoUrl) {
        this.status = VideoStatus.COMPLETED;
        this.videoUrl = finalVideoUrl;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = VideoStatus.FAILED;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getScript() { return script; }
    public VideoStatus getStatus() { return status; }
    public String getVideoUrl() { return videoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getBackgroundMusicUrl() { return backgroundMusicUrl; }
    public String getIntroVideoUrl() { return introVideoUrl; }
    public String getTopicTransitionUrl() { return topicTransitionUrl; }

    // Os setters dos arquivos opcionais foram removidos pois agora passamos via construtor!

    public void validateRequirements(Plan userPlan) {
        if (this.script == null || this.script.trim().isEmpty()) {
            throw new IllegalArgumentException("Script cannot be empty.");
        }

        // Regra específica: Plano Starter exige arquivos manuais
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