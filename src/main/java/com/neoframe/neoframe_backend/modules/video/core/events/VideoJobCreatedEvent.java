package com.neoframe.neoframe_backend.modules.video.core.events;

import java.util.UUID;

public record VideoJobCreatedEvent(
        UUID jobId,
        String inputData, // O JSON em string que o Front enviou, com as URLs
        String status
) {}