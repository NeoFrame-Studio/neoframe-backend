package com.neoframe.neoframe_backend.modules.video.infrastructure.webhook;

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

    // Construtor injeta os valores de timeout do application.properties e configura o RestTemplate
    public VideoWebhookNotifier(
            @Value("${app.worker.connect-timeout}") int connectTimeout,
            @Value("${app.worker.read-timeout}") int readTimeout) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); // Tempo limite para estabelecer conexão com o Hugging Face
        factory.setReadTimeout(readTimeout);       // Tempo limite aguardando a resposta do endpoint do Python

        this.restTemplate = new RestTemplate(factory);
    }

    @Async
    @EventListener
    public void onVideoJobCreated(VideoJobCreatedEvent event) {
        log.info("Sending Webhook notification to Python worker for job [{}]", event.jobId());

        try {
            // 1. Cria os headers explicitamente
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. Envelopa o evento com os headers
            HttpEntity<VideoJobCreatedEvent> requestEntity = new HttpEntity<>(event, headers);

            // 3. Usa o postForObject passando a requestEntity
            restTemplate.postForObject(pythonWorkerUrl, requestEntity, Void.class);

            log.info("Python worker successfully notified for job [{}].", event.jobId());
        } catch (Exception e) {
            log.error("Failed to notify Python worker for job [{}]. Reason: {}", event.jobId(), e.getMessage());
        }
    }
}