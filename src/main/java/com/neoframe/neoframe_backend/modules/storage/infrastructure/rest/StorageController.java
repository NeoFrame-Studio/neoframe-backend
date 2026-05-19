package com.neoframe.neoframe_backend.modules.storage.infrastructure.rest;

import com.neoframe.neoframe_backend.core.application.GenerateUploadUrlsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {

    private final GenerateUploadUrlsService generateUploadUrlsService;

    public StorageController(GenerateUploadUrlsService generateUploadUrlsService) {
        this.generateUploadUrlsService = generateUploadUrlsService;
    }

    @PostMapping("/upload-urls")
    public ResponseEntity<Map<String, Map<String, String>>> requestUploadUrls(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody UploadUrlRequest request) {

        UUID userId = UUID.fromString(authenticatedUserId);

        // request.fileTypes() could be: ["background_music.mp3", "intro_video.mp4", "transition.mp4"]
        Map<String, Map<String, String>> response = generateUploadUrlsService.execute(userId, request.fileTypes());

        return ResponseEntity.ok(response);
    }
}

record UploadUrlRequest(List<String> fileTypes) {}
