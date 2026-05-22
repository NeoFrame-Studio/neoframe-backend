package com.neoframe.neoframe_backend.shared.infrastructure.web.dto;

public class VideoJobDtos {

    // O frontend envia as URLs dos arquivos que foram feitos upload (ex: via AWS S3 ou Cloudflare R2)
    public record CreateJobRequest(
            String scriptUrl,
            String bgMusicUrl,
            String introUrl,
            String transitionUrl
    ) {}

    public record JobResponse(
            String jobId,
            String status,
            String message
    ) {}
}