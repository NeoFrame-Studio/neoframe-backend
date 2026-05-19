package com.neoframe.neoframe_backend.core.ports.out;

import java.util.Map;
import java.util.UUID;

public interface StoragePort {
    /**
     * Generates a pre-signed URL for direct frontend file upload.
     *
     * @param userId The ID of the user uploading the file (used for folder isolation)
     * @param fileName The name of the file to be uploaded
     * @return A map containing both the 'uploadUrl' (for React to PUT the file)
     *         and the 'finalUrl' (to save in the database later).
     */
    Map<String, String> generatePreSignedUploadUrl(UUID userId, String fileName);
}
