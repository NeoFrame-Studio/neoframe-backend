package com.neoframe.neoframe_backend.modules.video.core.events;

public record PathsPayload(
        String roteiro,
        String intro,
        String transicao,
        String musica
) {}