package com.neoframe.neoframe_backend.modules.video.core.events;

import java.util.List;
import java.util.UUID;

public record VideoCurationFinalizedEvent(
        UUID jobId,
        List<String> urlsEscolhidas
) {}
