package com.neoframe.neoframe_backend.modules.video.infrastructure.persistence;

import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.core.ports.out.VideoJobRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VideoJobRepositoryAdapter implements VideoJobRepositoryPort {

    private final VideoJobJpaRepository jpaRepository;

    public VideoJobRepositoryAdapter(VideoJobJpaRepository jpaRepository) {
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

    // Mapper: Converts Domain model to Database Entity
    private VideoJobEntity toEntity(VideoJob domain) {
        VideoJobEntity entity = new VideoJobEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setScript(domain.getScript());
        entity.setStatus(domain.getStatus());
        entity.setVideoUrl(domain.getVideoUrl());
        entity.setBackgroundMusicUrl(domain.getBackgroundMusicUrl());
        entity.setIntroVideoUrl(domain.getIntroVideoUrl());
        entity.setTopicTransitionUrl(domain.getTopicTransitionUrl());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        return entity;
    }

    // Mapper: Converts Database Entity back to Pure Domain model
    private VideoJob toDomain(VideoJobEntity entity) {
        VideoJob domain = new VideoJob(
                entity.getId(),
                entity.getUserId(),
                entity.getScript(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
        domain.setBackgroundMusicUrl(entity.getBackgroundMusicUrl());
        domain.setIntroVideoUrl(entity.getIntroVideoUrl());
        domain.setTopicTransitionUrl(entity.getTopicTransitionUrl());
        if (entity.getStatus() == VideoStatus.COMPLETED) {
            domain.complete(entity.getVideoUrl());
        }
        return domain;
    }
}
