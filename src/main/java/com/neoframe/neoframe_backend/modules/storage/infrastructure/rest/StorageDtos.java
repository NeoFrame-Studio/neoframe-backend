package com.neoframe.neoframe_backend.modules.storage.infrastructure.rest;

public class StorageDtos {

    public record PresignedUrlRequest(
            String fileName,
            String contentType,
            String type // 'script', 'music', 'intro', 'transition'
    ) {}

    public record PresignedUrlResponse(
            String uploadUrl,      // O link temporário do Supabase para o React fazer o upload
            String finalFileUrl    // O link público final onde o Python vai ler o arquivo depois
    ) {}
}