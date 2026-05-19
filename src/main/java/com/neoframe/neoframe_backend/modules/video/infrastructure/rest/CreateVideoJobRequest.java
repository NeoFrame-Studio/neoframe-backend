package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

// DTOs (Data Transfer Objects) mapping the exact JSON the React frontend will send/receive
record CreateVideoJobRequest(
        String script,
        String backgroundMusicUrl,
        String introVideoUrl,
        String topicTransitionUrl
) {
}

