package com.neoframe.neoframe_backend.shared.infrastructure.persistence.adapter;

import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.core.ports.out.VideoJobRepositoryPort;
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity.VideoJobEntity;
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.repository.SpringDataVideoJobRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class VideoJobPersistenceAdapter implements VideoJobRepositoryPort {

    private final SpringDataVideoJobRepository repository;

    public VideoJobPersistenceAdapter(SpringDataVideoJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public VideoJob save(VideoJob videoJob) {
        VideoJobEntity entity = toEntity(videoJob);
        VideoJobEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<VideoJob> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public int countActiveJobsByUserIdAndStatusIn(UUID userId, VideoStatus... statuses) {
        // Agora passamos a lista de Enums direto, pois o repositório foi ajustado para entender o tipo correto
        List<VideoStatus> statusList = Arrays.asList(statuses);
        return (int) repository.countByUserIdAndStatusIn(userId, statusList);
    }

    private VideoJob toDomain(VideoJobEntity entity) {
        // CORREÇÃO LINHA 53: Removido o VideoStatus.valueOf() pois entity.getStatus() já retorna o Enum correto
        VideoJob domain = new VideoJob(
                entity.getId(),
                entity.getUserId(),
                entity.getScript(),
                entity.getStatus(),
                entity.getCreatedAt()
        );

        // 2. Popula os campos opcionais do plano Starter que possuem setters
        domain.setBackgroundMusicUrl(entity.getBackgroundMusicUrl());
        domain.setIntroVideoUrl(entity.getIntroVideoUrl());
        domain.setTopicTransitionUrl(entity.getTopicTransitionUrl());

        // 3. Força a reconstrução dos campos de histórico privado usando reflexão pura
        try {
            Field videoUrlField = VideoJob.class.getDeclaredField("videoUrl");
            videoUrlField.setAccessible(true);
            videoUrlField.set(domain, entity.getVideoUrl());

            Field completedAtField = VideoJob.class.getDeclaredField("completedAt");
            completedAtField.setAccessible(true);
            completedAtField.set(domain, entity.getCompletedAt());
        } catch (Exception e) {
            throw new RuntimeException("Erro de infraestrutura ao restaurar o estado do VideoJob", e);
        }

        return domain;
    }

    private VideoJobEntity toEntity(VideoJob videoJob) {
        // CORREÇÃO LINHA 83: Passando o Enum direto (videoJob.getStatus()) em vez de mandar a String (.name())
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