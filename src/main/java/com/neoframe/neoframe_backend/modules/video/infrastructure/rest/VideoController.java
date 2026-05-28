package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.GenerateVideoUseCase;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.UpdateVideoJobStatusUseCase;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final GenerateVideoUseCase generateVideoUseCase;
    private final UpdateVideoJobStatusUseCase updateVideoJobStatusUseCase;

    // 1. ADICIONADO: Repositório para poder buscar o status do vídeo no GET
    private final VideoJobRepositoryPort videoJobRepository;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public VideoController(GenerateVideoUseCase generateVideoUseCase,
                           UpdateVideoJobStatusUseCase updateVideoJobStatusUseCase,
                           VideoJobRepositoryPort videoJobRepository) {
        this.generateVideoUseCase = generateVideoUseCase;
        this.updateVideoJobStatusUseCase = updateVideoJobStatusUseCase;
        this.videoJobRepository = videoJobRepository;
    }

    @PostMapping("/jobs")
    public ResponseEntity<VideoJobResponse> createVideoJob(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody CreateVideoJobRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);
        log.info("User [{}] requested video generation with clean JSON.", userId);

        UUID jobId = generateVideoUseCase.execute(
                userId,
                request.caminhos().roteiro(),
                request.caminhos().musica(),
                request.caminhos().intro(),
                request.caminhos().transicao()
        );

        log.info("Video job [{}] successfully enqueued.", jobId);
        return ResponseEntity.accepted().body(new VideoJobResponse(jobId, "Job successfully added to queue."));
    }

    // 2. ADICIONADO: O Endpoint de Polling que o Frontend (React) fica chamando
    @GetMapping("/{jobId}")
    public ResponseEntity<VideoJobResponseDTO> getStatus(@PathVariable UUID jobId) {
        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado: " + jobId));

        // Substituí o job.getProgress() por 0 diretamente
        VideoJobResponseDTO response = new VideoJobResponseDTO(
                job.getStatus().name(),
                0,
                job.getVideoUrl()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/finalize")
    public ResponseEntity<Void> finalizeCuration(
            @PathVariable UUID jobId,
            @RequestBody FinalizeCurationRequest request) {

        log.info("Recebido finalize para o job {} com {} urls", jobId, request.getUrlsEscolhidas().size());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/callback")
    public ResponseEntity<Void> workerCallback(
            @RequestHeader("X-Internal-Key") String incomingKey,
            @RequestBody WorkerCallbackRequest request) {

        if (!internalApiKey.equals(incomingKey)) {
            log.warn("Unauthorized access attempt to internal callback endpoint. Bad API Key.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Callback received from Python worker for job [{}]. Status: {}", request.jobId(), request.status());

        updateVideoJobStatusUseCase.execute(request.jobId(), request.status(), request.videoUrl());

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleRulesException(IllegalArgumentException ex) {
        log.warn("Business rule violation caught in Controller: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // 3. ADICIONADO: O formato de resposta (JSON) que o frontend espera receber no polling
    public record VideoJobResponseDTO(String status, int progress, String outputUrl) {}
}