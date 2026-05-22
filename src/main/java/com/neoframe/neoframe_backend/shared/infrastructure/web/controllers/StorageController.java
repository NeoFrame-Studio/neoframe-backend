package com.neoframe.neoframe_backend.shared.infrastructure.web.controllers;

import com.neoframe.neoframe_backend.shared.infrastructure.storage.dto.StorageDtos.*;
import com.neoframe.neoframe_backend.shared.infrastructure.storage.services.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = storageService.generatePresignedUrl(request);
        return ResponseEntity.ok(response);
    }
}