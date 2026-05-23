package com.neoframe.neoframe_backend.modules.video.core.ports.in;

import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;

public interface VideoJobUseCase {
    VideoJob createJob(String userEmail, String scriptUrl, String bgMusicUrl, String introUrl, String transitionUrl);
}