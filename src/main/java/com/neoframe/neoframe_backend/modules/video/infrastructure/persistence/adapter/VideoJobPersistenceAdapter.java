package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.adapter;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobEntity;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobJpaRepository;
import org.springframework.stereotype.Component;

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
        // Usa o segundo construtor (focado no repositório) para mapear o estado exato do banco
        return new VideoJob(
                entity.getId(),
                entity.getUserId(),
                entity.getScript(),
                entity.getStatus(),
                entity.getVideoUrl(), // Passa direto o URL do banco
                entity.getCreatedAt(),
                entity.getCompletedAt(), // Passa direto a data de conclusão
                entity.getBackgroundMusicUrl(),
                entity.getIntroVideoUrl(),
                entity.getTopicTransitionUrl()
        );
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