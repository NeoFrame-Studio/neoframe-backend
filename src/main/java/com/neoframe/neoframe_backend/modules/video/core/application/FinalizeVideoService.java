package com.neoframe.neoframe_backend.modules.video.core.application;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.events.VideoCurationFinalizedEvent;
import com.neoframe.neoframe_backend.modules.video.core.ports.in.FinalizeVideoUseCase;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FinalizeVideoService implements FinalizeVideoUseCase {

    private static final Logger log = LoggerFactory.getLogger(FinalizeVideoService.class);

    private final VideoJobRepositoryPort videoJobRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FinalizeVideoService(VideoJobRepositoryPort videoJobRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.videoJobRepository = videoJobRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional // Garante o commit no banco antes de liberar o fluxo HTTP
    public void execute(UUID jobId, List<String> urlsEscolhidas) {

        // 1. Busca o Job existente no banco de dados
        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado para finalização: " + jobId));

        log.info("Job [{}] encontrado. Status atual: {}. Executando finalização da curadoria.", jobId, job.getStatus());

        // 2. Executa a regra de transição de estado de Domínio (Muda o status internamente)
        job.finalizeCuration();

        // 3. Salva a alteração no banco (O Spring Data faz o Update usando o ID existente)
        videoJobRepository.save(job);
        log.info("Job [{}] salvo com sucesso no banco com o novo status: PROCESSING.", jobId);

        // 4. Dispara o evento para o listener de infraestrutura chamar o Python em background
        log.info("Disparando evento de curadoria finalizada para o Job [{}]", jobId);
        eventPublisher.publishEvent(new VideoCurationFinalizedEvent(jobId, urlsEscolhidas));
    }
}