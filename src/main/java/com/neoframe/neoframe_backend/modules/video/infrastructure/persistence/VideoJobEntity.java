package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_jobs")
@Getter
@Setter
public class VideoJobEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String script;

    // Mapeia o Enum para ser gravado como texto (VARCHAR) puro no PostgreSQL/MySQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "background_music_url")
    private String backgroundMusicUrl;

    @Column(name = "intro_video_url")
    private String introVideoUrl;

    @Column(name = "topic_transition_url")
    private String topicTransitionUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Construtor padrão obrigatório exigido pelo JPA/Hibernate para instanciar a entidade
     */
    public VideoJobEntity() {}

    /**
     * Construtor completo usado pelo seu Database Adapter para converter
     * o objeto de Domínio do Core para esta Entidade de infraestrutura.
     */
    public VideoJobEntity(UUID id, UUID userId, String script, VideoStatus status, String videoUrl,
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
}