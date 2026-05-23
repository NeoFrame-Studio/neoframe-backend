package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.adapter;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobEntity;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobJpaRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VideoJobPersistenceAdapter implements VideoJobRepositoryPort {

    private final VideoJobJpaRepository jpaRepository;

    public VideoJobPersistenceAdapter(VideoJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VideoJob save(VideoJob videoJob) {
        VideoJobEntity entity = toEntity(videoJob);
        VideoJobEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<VideoJob> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public int countActiveJobsByUserIdAndStatusIn(UUID userId, VideoStatus... statuses) {
        List<VideoStatus> statusList = Arrays.asList(statuses);
        return jpaRepository.countByUserIdAndStatusIn(userId, statusList);
    }

    /**
     * Mapeador: Converte a Entidade de Banco de Dados de volta para o Modelo de Domínio Puro
     */
    private VideoJob toDomain(VideoJobEntity entity) {
        // 1. Instancia o núcleo imutável do domínio
        VideoJob domain = new VideoJob(
                entity.getId(),
                entity.getUserId(),
                entity.getScript(),
                entity.getStatus(),
                entity.getCreatedAt()
        );

        // 2. Popula os campos opcionais do plano Starter usando os setters expostos
        domain.setBackgroundMusicUrl(entity.getBackgroundMusicUrl());
        domain.setIntroVideoUrl(entity.getIntroVideoUrl());
        domain.setTopicTransitionUrl(entity.getTopicTransitionUrl());

        // 3. Executa lógica de negócio rica se o job estiver finalizado com sucesso
        if (entity.getStatus() == VideoStatus.COMPLETED && entity.getVideoUrl() != null) {
            domain.complete(entity.getVideoUrl());
        }

        // 4. Mecanismo de Segurança: Força a restauração exata dos campos privados do histórico via Reflexão
        // Isso garante consistência total se o objeto vier do banco em estados como falhas ou timeouts.
        try {
            Field videoUrlField = VideoJob.class.getDeclaredField("videoUrl");
            videoUrlField.setAccessible(true);
            videoUrlField.set(domain, entity.getVideoUrl());

            Field completedAtField = VideoJob.class.getDeclaredField("completedAt");
            completedAtField.setAccessible(true);
            completedAtField.set(domain, entity.getCompletedAt());
        } catch (Exception e) {
            throw new RuntimeException("Erro de infraestrutura ao restaurar o estado completo do VideoJob", e);
        }

        return domain;
    }

    /**
     * Mapeador: Converte o Modelo de Domínio para a Entidade Relacional do JPA
     */
    private VideoJobEntity toEntity(VideoJob videoJob) {
        return new VideoJobEntity(
                videoJob.getId(),
                videoJob.getUserId(),
                videoJob.getScript(),
                videoJob.getStatus(),
                videoJob.getVideoUrl(),
                videoJob.getCreatedAt(),
                videoJob.getCompletedAt(),
                videoJob.getBackgroundMusicUrl(),
                videoJob.getIntroVideoUrl(),
                videoJob.getTopicTransitionUrl()
        );
    }
}