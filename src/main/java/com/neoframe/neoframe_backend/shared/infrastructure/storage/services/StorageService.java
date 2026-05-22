package com.neoframe.neoframe_backend.shared.infrastructure.storage.services;

import com.neoframe.neoframe_backend.shared.infrastructure.storage.dto.StorageDtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucketName;
    private final RestTemplate restTemplate;

    public StorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey,
            @Value("${supabase.bucket}") String bucketName) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.bucketName = bucketName;
        this.restTemplate = new RestTemplate();
    }

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        // 1. Gera um nome de arquivo único para não sobrescrever uploads antigos
        String extension = request.fileName().substring(request.fileName().lastIndexOf("."));
        String filePath = "uploads/" + request.type() + "/" + UUID.randomUUID() + extension;

        // 2. Endpoint da API REST do Supabase para gerar o link de upload seguro
        String apiUrl = supabaseUrl + "/storage/v1/object/upload/sign/" + bucketName + "/" + filePath;

        // 3. Monta os cabeçalhos com a sua chave secreta do Supabase
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // 4. Bate no Supabase pedindo a URL
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            // O Supabase devolve algo como: {"url": "/object/upload/sign/...token..."}
            String relativeUploadUrl = (String) response.getBody().get("url");

            // 5. Monta a URL completa para o React fazer o PUT
            String uploadUrl = supabaseUrl + "/storage/v1" + relativeUploadUrl;

            // 6. Monta a URL pública final que será enviada para o banco/Python
            String finalFileUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;

            return new PresignedUrlResponse(uploadUrl, finalFileUrl);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar URL do Supabase: " + e.getMessage());
        }
    }
}