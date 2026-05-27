package com.neoframe.neoframe_backend.modules.video.core.application;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.UpdateVideoJobStatusUseCase;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateVideoJobStatusService implements UpdateVideoJobStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateVideoJobStatusService.class);
    private final VideoJobRepositoryPort videoJobRepository;

    public UpdateVideoJobStatusService(VideoJobRepositoryPort videoJobRepository) {
        this.videoJobRepository = videoJobRepository;
    }

    @Override
    public void execute(UUID jobId, String status, String videoUrl) {
        // 1. Busca o Job no banco de dados
        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Video job not found with ID: " + jobId));

        log.info("Updating status for job [{}]. Old: {} -> New: {}", jobId, job.getStatus(), status);

        // 2. Converte a string do Python para Enum
        VideoStatus newStatus;
        try {
            newStatus = VideoStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status provided by worker: " + status);
        }

        // 3. Usa os métodos de comportamento do seu Domínio (Rich Domain)
        switch (newStatus) {
            case PROCESSING -> job.startProcessing();
            case WAITING_CURATION -> job.readyForCuration(videoUrl);
            case COMPLETED -> job.complete(videoUrl);
            case FAILED -> job.fail();
            case PENDING -> log.info("Job [{}] is already pending.", jobId);
        }

        // 4. Salva as alterações
        videoJobRepository.save(job);

        log.info("Job [{}] successfully updated to {} status.", jobId, job.getStatus());
    }
}