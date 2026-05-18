package com.neoframe.neoframe_backend.modules.video.infrastructure.jobs;

import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.infrastructure.persistence.VideoJobEntity;
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

    // Define the maximum allowed time for a video to be processed (e.g., 30 minutes)
    private static final int TIMEOUT_MINUTES = 30;

    public VideoJobTimeoutScheduler(VideoJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // Runs automatically every 5 minutes
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupStuckJobs() {
        log.info("Running timeout guardian: Checking for stuck video processing jobs...");

        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

        // Find all jobs that are stuck in PROCESSING state for too long
        // (You would add a custom query in the JpaRepository for this:
        // findByStatusAndCreatedAtBefore(VideoStatus.PROCESSING, thresholdTime))
        List<VideoJobEntity> stuckJobs = jpaRepository.findStuckJobs(VideoStatus.PROCESSING, thresholdTime);

        if (stuckJobs.isEmpty()) {
            log.debug("No stuck jobs found. Everything is running smoothly.");
            return;
        }

        for (VideoJobEntity job : stuckJobs) {
            log.warn("Job {} for User {} timed out. Changing status to FAILED to free up user queue slot.",
                    job.getId(), job.getUserId());

            job.setStatus(VideoStatus.FAILED);
            jpaRepository.save(job);
        }
    }
}
