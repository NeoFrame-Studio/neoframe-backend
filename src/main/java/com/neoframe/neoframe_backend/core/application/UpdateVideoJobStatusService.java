package com.neoframe.neoframe_backend.core.application;

import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.core.ports.in.UpdateVideoJobStatusUseCase;
import com.neoframe.neoframe_backend.core.ports.out.VideoJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        // Busca o Job no banco de dados
        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Video job not found with ID: " + jobId));

        log.info("Updating status for job [{}]. Old: {} -> New: {}", jobId, job.getStatus(), status);

        try {
            // Converte a string recebida do Python para o Enum do Java (PENDING, PROCESSING, COMPLETED, FAILED)
            VideoStatus newStatus = VideoStatus.valueOf(status.toUpperCase());
            job.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status provided by worker: " + status);
        }

        // Se o processamento deu certo, vincula o link final do Supabase
        if (job.getStatus() == VideoStatus.COMPLETED) {
            job.setVideoUrl(videoUrl);
        }

        // Grava o carimbo de data/hora do término
        job.setCompletedAt(LocalDateTime.now());

        // Salva as alterações através do adaptador do banco
        videoJobRepository.save(job);

        log.info("Job [{}] successfully updated to {} status.", jobId, job.getStatus());
    }
}
