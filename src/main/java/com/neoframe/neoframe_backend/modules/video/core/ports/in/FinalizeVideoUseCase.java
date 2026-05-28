package com.neoframe.neoframe_backend.modules.video.core.ports.in;

import java.util.List;
import java.util.UUID;

public interface FinalizeVideoUseCase {
    void execute(UUID jobId, List<String> urlsEscolhidas);
}