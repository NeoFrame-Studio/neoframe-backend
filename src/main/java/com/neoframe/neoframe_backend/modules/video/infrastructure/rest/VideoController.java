package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import com.neoframe.neoframe_backend.modules.video.core.ports.in.GenerateVideoUseCase;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.UpdateVideoJobStatusUseCase;
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

    // Chave secreta de comunicação segura com o Worker Python (Hugging Face)
    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public VideoController(GenerateVideoUseCase generateVideoUseCase,
                           UpdateVideoJobStatusUseCase updateVideoJobStatusUseCase) {
        this.generateVideoUseCase = generateVideoUseCase;
        this.updateVideoJobStatusUseCase = updateVideoJobStatusUseCase;
    }

    /**
     * Rota usada pelo Frontend React para solicitar a geração de um vídeo.
     * Mapeia os dados informados (incluindo opcionais do plano Starter se houver) e põe na fila.
     */
    @PostMapping("/jobs")
    public ResponseEntity<VideoJobResponse> createVideoJob(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody CreateVideoJobRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);
        log.info("User [{}] requested video generation.", userId);

        // Executa o caso de uso injetando os parâmetros enviados pelo Frontend
        UUID jobId = generateVideoUseCase.execute(
                userId,
                request.script(),
                request.backgroundMusicUrl(),
                request.introVideoUrl(),
                request.topicTransitionUrl()
        );

        log.info("Video job [{}] successfully enqueued for user [{}].", jobId, userId);

        // Retorna o HTTP 202 Accepted, ideal para processamentos assíncronos/filas
        return ResponseEntity.accepted().body(new VideoJobResponse(jobId, "Job successfully added to queue."));
    }

    /**
     * Rota de Callback protegida usada exclusivamente pelo Worker Python para atualizar o status do processamento.
     */
    @PostMapping("/internal/callback")
    public ResponseEntity<Void> workerCallback(
            @RequestHeader("X-Internal-Key") String incomingKey,
            @RequestBody WorkerCallbackRequest request) {

        // Validação de segurança: barra a requisição se o token do cabeçalho não bater com a aplicação
        if (!internalApiKey.equals(incomingKey)) {
            log.warn("Unauthorized access attempt to internal callback endpoint. Bad API Key.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Callback received from Python worker for job [{}]. Status: {}", request.jobId(), request.status());

        // Atualiza o estado da entidade no banco via UseCase
        updateVideoJobStatusUseCase.execute(request.jobId(), request.status(), request.videoUrl());

        return ResponseEntity.ok().build();
    }

    /**
     * Captura exceções de validação de plano ou regras de negócio estouradas no Core
     * e devolve uma resposta amigável estruturada em formato JSON para o seu frontend.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleRulesException(IllegalArgumentException ex) {
        log.warn("Business rule violation caught in Controller: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}