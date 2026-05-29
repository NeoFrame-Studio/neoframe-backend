package com.neoframe.neoframe_backend.modules.video.core.events;

import java.util.List;
import java.util.UUID;

// O record agora reflete exatamente o que o Python espera
public record VideoCurationFinalizedEvent(
        UUID jobId,
        String status,
        PathsPayload caminhos,
        List<String> urlsEscolhidas
) {}