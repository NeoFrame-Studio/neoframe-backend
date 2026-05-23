package com.neoframe.neoframe_backend.modules.video.core.application;


import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.events.VideoJobCreatedEvent;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.GenerateVideoUseCase;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import org.springframework.context.ApplicationEventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GenerateVideoService implements GenerateVideoUseCase {

    // Inicialização do Logger para regras de negócio do Core
    private static final Logger log = LoggerFactory.getLogger(GenerateVideoService.class);

    private final UserRepositoryPort userRepository;
    private final VideoJobRepositoryPort videoJobRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Construtor manual que resolve o erro "Field might not have been initialized"
    public GenerateVideoService(UserRepositoryPort userRepository,
                                VideoJobRepositoryPort videoJobRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.videoJobRepository = videoJobRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UUID execute(UUID userId, String script, String backgroundMusicUrl, String introVideoUrl, String topicTransitionUrl) {

        // Busca o usuário dono da requisição
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Conta quantos vídeos ele tem rodando agora no banco
        int activeJobs = videoJobRepository.countActiveJobsByUserIdAndStatusIn(
                userId,
                VideoStatus.PENDING,
                VideoStatus.PROCESSING
        );

        // Valida a regra de concorrência baseada no plano do SaaS
        if (activeJobs >= user.getPlan().getMaxConcurrentJobs()) {
            // LOG DE ALERTA: Registra a tentativa de abuso de concorrência antes de estourar o erro
            log.warn("User [{}] blocked from creating a job. Reason: Max concurrent jobs reached ({}/{})",
                    userId, activeJobs, user.getPlan().getMaxConcurrentJobs());

            throw new IllegalStateException("You have reached the maximum number of concurrent video generations for your plan.");
        }

        // Cria e popula o novo Job de vídeo
        VideoJob newJob = new VideoJob(UUID.randomUUID(), userId, script, VideoStatus.PENDING, LocalDateTime.now());
        newJob.setBackgroundMusicUrl(backgroundMusicUrl);
        newJob.setIntroVideoUrl(introVideoUrl);
        newJob.setTopicTransitionUrl(topicTransitionUrl);

        // Salva as alterações no banco através do adaptador
        VideoJob savedJob = videoJobRepository.save(newJob);

        // Dispara o evento assíncrono para o ecossistema (avisar o webhook do Python)
        eventPublisher.publishEvent(new VideoJobCreatedEvent(savedJob.getId()));

        return savedJob.getId();
    }
}