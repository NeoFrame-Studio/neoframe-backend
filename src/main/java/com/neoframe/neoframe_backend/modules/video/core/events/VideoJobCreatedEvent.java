package com.neoframe.neoframe_backend.modules.video.core.events;

import java.util.UUID;

public record VideoJobCreatedEvent(
        UUID jobId,
        String status,
        PathsPayload caminhos
) {}