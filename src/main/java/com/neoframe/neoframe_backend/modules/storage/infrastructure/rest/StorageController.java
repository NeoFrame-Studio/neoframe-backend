package com.neoframe.neoframe_backend.modules.storage.infrastructure.rest;

import com.neoframe.neoframe_backend.modules.storage.core.application.GenerateUploadUrlsService;
import com.neoframe.neoframe_backend.modules.storage.core.application.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private static final Logger log = LoggerFactory.getLogger(StorageController.class);

    private final GenerateUploadUrlsService generateUploadUrlsService;
    private final StorageService storageService;

    public StorageController(GenerateUploadUrlsService generateUploadUrlsService,
                             StorageService storageService) {
        this.generateUploadUrlsService = generateUploadUrlsService;
        this.storageService = storageService;
    }

    /**
     * Rota em lote usada pelo Frontend React para solicitar URLs de upload para múltiplos arquivos.
     * Perfeito para o plano Starter carregar os 4 arquivos obrigatórios de uma só vez.
     */
    @PostMapping("/upload-urls")
    public ResponseEntity<Map<String, Map<String, String>>> requestUploadUrls(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody UploadUrlRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);
        log.info("User [{}] requested batch upload URLs for assets: {}", userId, request.fileTypes());

        // Retorna o mapeamento contendo a URL de upload pré-assinada gerada para cada tipo de arquivo
        Map<String, Map<String, String>> response = generateUploadUrlsService.execute(userId, request.fileTypes());

        return ResponseEntity.ok(response);
    }

    /**
     * Rota pontual para geração de uma única URL assinada (Presigned URL) para o Supabase Storage.
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<com.neoframe.neoframe_backend.modules.storage.infrastructure.rest.StorageDtos.PresignedUrlResponse> getPresignedUrl(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody PresignedUrlRequest request) {

        log.info("User [{}] requested a single presigned URL for file: {} [type: {}]",
                authenticatedUserId, request.fileName(), request.type());

        // Monta o DTO legado com os 3 argumentos exatos que o seu StorageService exige
        com.neoframe.neoframe_backend.modules.storage.infrastructure.rest.StorageDtos.PresignedUrlRequest legacyRequest =
                new com.neoframe.neoframe_backend.modules.storage.infrastructure.rest.StorageDtos.PresignedUrlRequest(
                        request.fileName(),
                        request.contentType(),
                        request.type()
                );

        com.neoframe.neoframe_backend.modules.storage.infrastructure.rest.StorageDtos.PresignedUrlResponse response =
                storageService.generatePresignedUrl(legacyRequest);

        return ResponseEntity.ok(response);
    }
}

// =========================================================================
// DTOs CONCENTRADOS (Substitua os antigos do final do arquivo por estes)
// =========================================================================
record UploadUrlRequest(List<String> fileTypes) {}
record PresignedUrlRequest(String fileName, String contentType, String type) {}