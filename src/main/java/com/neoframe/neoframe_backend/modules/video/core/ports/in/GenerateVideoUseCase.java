package com.neoframe.neoframe_backend.modules.video.core.ports.in;

import java.util.UUID;

public interface GenerateVideoUseCase {

    UUID execute(UUID userId, String script, String backgroundMusicUrl,
                 String introVideoUrl, String topicTransitionUrl);

}
