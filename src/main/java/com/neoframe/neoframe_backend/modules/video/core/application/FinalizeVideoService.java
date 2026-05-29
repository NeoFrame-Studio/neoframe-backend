package com.neoframe.neoframe_backend.modules.video.core.application;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.events.PathsPayload;
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
    @Transactional
    public void execute(UUID jobId, List<String> urlsEscolhidas) {

        VideoJob job = videoJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado para finalização: " + jobId));

        log.info("Job [{}] encontrado. Status atual: {}. Executando finalização da curadoria.", jobId, job.getStatus());

        job.finalizeCuration();
        videoJobRepository.save(job);
        log.info("Job [{}] salvo com sucesso no banco com o novo status: PROCESSING.", jobId);

        PathsPayload caminhos = new PathsPayload(
                job.getScript(),
                job.getIntroVideoUrl(),
                job.getTopicTransitionUrl(),
                job.getBackgroundMusicUrl()
        );

        log.info("Disparando evento de curadoria finalizada para o Job [{}]", jobId);

        eventPublisher.publishEvent(new VideoCurationFinalizedEvent(
                jobId,
                job.getStatus().name(),
                caminhos,
                urlsEscolhidas
        ));
    }
}