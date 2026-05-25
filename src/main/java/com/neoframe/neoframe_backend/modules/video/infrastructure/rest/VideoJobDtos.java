package com.neoframe.neoframe_backend.modules.video.infrastructure.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoJobDtos {

    public record PathsDto(
            String roteiro,
            String intro,
            String transicao,
            String musica
    ) {}

    public record JobResponse(
            String jobId,
            String status,
            String message
    ) {}
}