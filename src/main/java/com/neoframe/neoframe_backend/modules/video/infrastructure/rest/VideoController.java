package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import com.neoframe.neoframe_backend.core.ports.in.GenerateVideoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final GenerateVideoUseCase generateVideoUseCase;

    public VideoController(GenerateVideoUseCase generateVideoUseCase) {
        this.generateVideoUseCase = generateVideoUseCase;
    }

    @PostMapping("/jobs")
    public ResponseEntity<VideoJobResponse> createVideoJob(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody CreateVideoJobRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);

        // Dispatches the command to the Core Domain
        UUID jobId = generateVideoUseCase.execute(
                userId,
                request.script(),
                request.backgroundMusicUrl(),
                request.introVideoUrl(),
                request.topicTransitionUrl()
        );

        return ResponseEntity.accepted().body(new VideoJobResponse(jobId, "Job successfully added to the processing queue."));
    }
}

// DTOs (Data Transfer Objects) mapping the exact JSON the React frontend will send/receive
record CreateVideoJobRequest(
        String script,
        String backgroundMusicUrl,
        String introVideoUrl,
        String topicTransitionUrl
) {}

record VideoJobResponse(
        UUID jobId,
        String message
) {}
