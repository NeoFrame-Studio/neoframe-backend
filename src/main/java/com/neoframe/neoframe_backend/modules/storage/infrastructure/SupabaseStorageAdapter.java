package com.neoframe.neoframe_backend.modules.storage.infrastructure;

import com.neoframe.neoframe_backend.core.ports.out.StoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SupabaseStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    // These values will be injected from application.yml / Railway environment variables
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucketName;

    @Override
    public Map<String, String> generatePreSignedUploadUrl(UUID userId, String fileName) {
        // The path structure in the bucket: /userId/fileName
        String filePath = userId.toString() + "/" + fileName;

        log.info("Generating pre-signed URL for file path: {}", filePath);

        // TODO: In a real implementation, you will use Spring's RestClient or WebClient
        // to call Supabase REST API:
        // POST /storage/v1/object/sign/{bucketName}/{filePath}
        // with the Authorization header containing the supabaseServiceKey.

        // For architectural setup, we simulate the returned URLs.
        String mockedUploadUrl = supabaseUrl + "/storage/v1/object/upload/sign/" + bucketName + "/" + filePath + "?token=xyz123";
        String mockedFinalUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;

        Map<String, String> urls = new HashMap<>();
        urls.put("uploadUrl", mockedUploadUrl); // React uses this to PUT the binary file
        urls.put("finalUrl", mockedFinalUrl);   // React sends this back to the VideoController later

        return urls;
    }
}
