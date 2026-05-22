package com.neoframe.neoframe_backend.modules.video.infrastructure.jobs;

import com.neoframe.neoframe_backend.core.domain.VideoStatus;
// CORREÇÃO AQUI: Mudamos o import para apontar para a entidade correta do shared
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity.VideoJobEntity;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class VideoJobTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(VideoJobTimeoutScheduler.class);
    private final VideoJobJpaRepository jpaRepository;

    // Define o tempo máximo permitido para processar um vídeo (30 minutos)
    private static final int TIMEOUT_MINUTES = 30;

    public VideoJobTimeoutScheduler(VideoJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // Roda automaticamente a cada 5 minutos
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupStuckJobs() {
        log.info("Running timeout guardian: Checking for stuck video processing jobs...");

        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

        // Busca os jobs travados usando o repositório ajustado
        List<VideoJobEntity> stuckJobs = jpaRepository.findStuckJobs(VideoStatus.PROCESSING, thresholdTime);

        if (stuckJobs.isEmpty()) {
            log.debug("No stuck jobs found. Everything is running smoothly.");
            return;
        }

        for (VideoJobEntity job : stuckJobs) {
            log.warn("Job {} for User {} timed out. Changing status to FAILED to free up user queue slot.",
                    job.getId(), job.getUserId());

            // Aqui funciona direto porque ajustamos o VideoJobEntity para usar o Enum VideoStatus puro!
            job.setStatus(VideoStatus.FAILED);
            jpaRepository.save(job);
        }
    }
}