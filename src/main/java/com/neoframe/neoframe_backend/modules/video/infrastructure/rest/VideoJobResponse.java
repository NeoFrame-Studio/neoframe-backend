package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import java.util.UUID;

public record VideoJobResponse(
        UUID jobId,
        String message
) {
}
