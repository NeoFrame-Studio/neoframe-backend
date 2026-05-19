package com.neoframe.neoframe_backend.core.application;

import com.neoframe.neoframe_backend.core.ports.out.StoragePort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GenerateUploadUrlsService {

    private final StoragePort storagePort;

    public GenerateUploadUrlsService(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    public Map<String, Map<String, String>> execute(UUID userId, List<String> fileTypes) {
        Map<String, Map<String, String>> uploadSession = new HashMap<>();

        // Unique folder/prefix for this specific upload batch to prevent overwriting
        String batchId = UUID.randomUUID().toString();

        for (String type : fileTypes) {
            // Example: "intro_video" -> "batchId_intro_video.mp4"
            String uniqueFileName = batchId + "_" + type;

            Map<String, String> urls = storagePort.generatePreSignedUploadUrl(userId, uniqueFileName);
            uploadSession.put(type, urls);
        }

        return uploadSession;
    }
}
