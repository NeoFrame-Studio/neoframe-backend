package com.neoframe.neoframe_backend.core.application;

import com.neoframe.neoframe_backend.core.domain.Plan;
import com.neoframe.neoframe_backend.core.domain.User;
import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.core.ports.in.GenerateVideoUseCase;
import com.neoframe.neoframe_backend.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.core.ports.out.VideoJobRepositoryPort;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenerateVideoService implements GenerateVideoUseCase {

    private final UserRepositoryPort userRepository;
    private final VideoJobRepositoryPort videoJobRepository;

    public GenerateVideoService(UserRepositoryPort userRepository, VideoJobRepositoryPort videoJobRepository) {
        this.userRepository = userRepository;
        this.videoJobRepository = videoJobRepository;
    }

    @Override
    public UUID execute(UUID userId, String script, String backgroundMusicUrl,
                        String introVideoUrl, String topicTransitionUrl) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // 1. Check concurrency limits based on the user's plan
        int activeJobs = videoJobRepository.countActiveJobsByUserIdAndStatusIn(
                userId, VideoStatus.PENDING, VideoStatus.PROCESSING
        );

        if (activeJobs >= user.getPlan().getMaxConcurrentJobs()) {
            throw new IllegalStateException("You have reached the maximum number of concurrent video generations for your plan.");
        }

        // 2. Create the Job
        VideoJob newJob = new VideoJob(
                UUID.randomUUID(),
                userId,
                script,
                VideoStatus.PENDING,
                LocalDateTime.now()
        );

        newJob.setBackgroundMusicUrl(backgroundMusicUrl);
        newJob.setIntroVideoUrl(introVideoUrl);
        newJob.setTopicTransitionUrl(topicTransitionUrl);

        // 3. Domain rule validation (Starter plan missing assets, empty script, etc.)
        newJob.validateRequirements(user.getPlan());

        // 4. Persist the Job and return the tracking ID to the frontend
        VideoJob savedJob = videoJobRepository.save(newJob);

        // TODO: Dispatch Async Event to actually start the FFmpeg/Python processing

        return savedJob.getId();
    }
}
