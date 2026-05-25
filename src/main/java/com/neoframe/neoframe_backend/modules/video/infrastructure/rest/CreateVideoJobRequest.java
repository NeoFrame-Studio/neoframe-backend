package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

// DTOs (Data Transfer Objects) mapping the exact JSON the React frontend will send/receive
public record CreateVideoJobRequest(
        VideoJobDtos.PathsDto caminhos,
        String tema,
        String modo,
        String token
) {}

