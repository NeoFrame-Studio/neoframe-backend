package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import com.neoframe.neoframe_backend.core.ports.in.GenerateVideoUseCase;
import com.neoframe.neoframe_backend.core.ports.in.UpdateVideoJobStatusUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final GenerateVideoUseCase generateVideoUseCase;
    private final UpdateVideoJobStatusUseCase updateVideoJobStatusUseCase;

    // Chave secreta de comunicação entre os seus dois servidores
    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public VideoController(GenerateVideoUseCase generateVideoUseCase,
                           UpdateVideoJobStatusUseCase updateVideoJobStatusUseCase) {
        this.generateVideoUseCase = generateVideoUseCase;
        this.updateVideoJobStatusUseCase = updateVideoJobStatusUseCase;
    }

    // Rota usada pelo Frontend React para mandar o roteiro
    @PostMapping("/jobs")
    public ResponseEntity<VideoJobResponse> createVideoJob(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody CreateVideoJobRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);
        log.info("User [{}] requested video generation.", userId);

        UUID jobId = generateVideoUseCase.execute(
                userId, request.script(), request.backgroundMusicUrl(),
                request.introVideoUrl(), request.topicTransitionUrl()
        );

        log.info("Video job [{}] successfully enqueued for user [{}].", jobId, userId);
        return ResponseEntity.accepted().body(new VideoJobResponse(jobId, "Job successfully added to queue."));
    }

    // Rota usada exclusivamente pelo Worker Python no Hugging Face para devolver o resultado
    @PostMapping("/internal/callback")
    public ResponseEntity<Void> workerCallback(
            @RequestHeader("X-Internal-Key") String incomingKey,
            @RequestBody WorkerCallbackRequest request) {

        // Segurança básica: Se a chave enviada pelo Python não bater com a do Java, barra na hora
        if (!internalApiKey.equals(incomingKey)) {
            log.warn("Unauthorized access attempt to internal callback endpoint from IP or bad token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Callback received from Python worker for job [{}]. Status: {}", request.jobId(), request.status());

        updateVideoJobStatusUseCase.execute(request.jobId(), request.status(), request.videoUrl());

        return ResponseEntity.ok().build();
    }
}