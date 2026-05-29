package com.neoframe.neoframe_backend.modules.video.infrastructure.webhook;

import com.neoframe.neoframe_backend.modules.video.core.events.VideoCurationFinalizedEvent;
import com.neoframe.neoframe_backend.modules.video.core.events.VideoJobCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class VideoWebhookNotifier {

    private static final Logger log = LoggerFactory.getLogger(VideoWebhookNotifier.class);

    private final RestTemplate restTemplate;

    @Value("${app.worker.python-url}")
    private String pythonWorkerUrl;

    // Construtor injeta os valores de timeout...
    public VideoWebhookNotifier(
            @Value("${app.worker.connect-timeout}") int connectTimeout,
            @Value("${app.worker.read-timeout}") int readTimeout) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        this.restTemplate = new RestTemplate(factory);
    }

    // --- LISTENER DA PARTE A (INÍCIO DO PROCESSO) ---
    @Async
    @EventListener
    public void onVideoJobCreated(VideoJobCreatedEvent event) {
        log.info("Sending Webhook notification to Python worker for job [{}]", event.jobId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<VideoJobCreatedEvent> requestEntity = new HttpEntity<>(event, headers);

            // Supondo que a raiz da URL inicie a Parte A
            restTemplate.postForObject(pythonWorkerUrl, requestEntity, Void.class);

            log.info("Python worker successfully notified for job [{}].", event.jobId());
        } catch (Exception e) {
            log.error("Failed to notify Python worker for job [{}]. Reason: {}", event.jobId(), e.getMessage());
        }
    }

    // --- NOVO: LISTENER DA PARTE B (APÓS CURADORIA) ---
    @Async
    @EventListener
    public void onVideoJobCurated(VideoCurationFinalizedEvent event) {
        log.info("Sending Webhook notification for CURATION FINALIZED to Python worker for job [{}]", event.jobId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<VideoCurationFinalizedEvent> requestEntity = new HttpEntity<>(event, headers);

            // IMPORTANTE: Adicionei o sufixo "/finalize" (ou "/render", ou o que você usou no Python)
            // para que a sua API em Python saiba diferenciar a Parte A da Parte B.
            // Se o Python trata na mesma rota (apenas checando status), você pode manter só o pythonWorkerUrl
            String pythonRenderUrl = pythonWorkerUrl + "/jobs";

            restTemplate.postForObject(pythonRenderUrl, requestEntity, Void.class);

            log.info("Python worker successfully notified to start RENDER for job [{}].", event.jobId());
        } catch (Exception e) {
            log.error("Failed to notify Python worker for CURATION FINALIZED on job [{}]. Reason: {}", event.jobId(), e.getMessage());
        }
    }
}