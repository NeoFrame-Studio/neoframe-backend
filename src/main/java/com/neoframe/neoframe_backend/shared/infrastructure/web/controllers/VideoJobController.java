package com.neoframe.neoframe_backend.shared.infrastructure.web.controllers;

import com.neoframe.neoframe_backend.core.domain.VideoJob;
import com.neoframe.neoframe_backend.core.ports.in.VideoJobUseCase;
import com.neoframe.neoframe_backend.shared.infrastructure.web.dto.VideoJobDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/videos")
public class VideoJobController {

    private final VideoJobUseCase videoJobUseCase;

    public VideoJobController(VideoJobUseCase videoJobUseCase) {
        this.videoJobUseCase = videoJobUseCase;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createVideoJob(@RequestBody CreateJobRequest request) {
        // Pega o e-mail do usuário que o filtro JWT injetou no contexto de segurança
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        VideoJob job = videoJobUseCase.createJob(
                userEmail,
                request.scriptUrl(),
                request.bgMusicUrl(),
                request.introUrl(),
                request.transitionUrl()
        );

        JobResponse response = new JobResponse(
                job.getId().toString(),
                job.getStatus().name(),
                "Vídeo adicionado à fila de processamento com sucesso!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Trata erros de plano ou limite excedido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleRulesException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}