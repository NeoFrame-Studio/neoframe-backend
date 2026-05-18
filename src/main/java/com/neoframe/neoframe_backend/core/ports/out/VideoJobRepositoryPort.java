package com.neoframe.neoframe_backend.core.ports.out;

import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.domain.VideoStatus;

import java.util.Optional;
import java.util.UUID;

public interface VideoJobRepositoryPort {
    VideoJob save(VideoJob videoJob);
    Optional<VideoJob> findById(UUID id);

    // Crucial for validating the concurrency limit
    int countActiveJobsByUserIdAndStatusIn(UUID userId, VideoStatus... statuses);
}
