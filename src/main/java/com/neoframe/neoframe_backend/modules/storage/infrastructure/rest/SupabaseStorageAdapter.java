package com.neoframe.neoframe_backend.modules.storage.infrastructure.rest;

import com.neoframe.neoframe_backend.modules.storage.core.ports.out.StoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SupabaseStorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucketName;

    // Adicione a sua service role key no seu application.properties ou variáveis do Railway
    @Value("${supabase.key}")
    private String supabaseServiceKey;

    private final RestClient restClient;

    // Inicializa o cliente HTTP do Spring
    public SupabaseStorageAdapter() {
        this.restClient = RestClient.builder().build();
    }

    @Override
    public Map<String, String> generatePreSignedUploadUrl(UUID userId, String fileName) {
        String filePath = userId.toString() + "/" + fileName;
        log.info("Requesting real pre-signed URL from Supabase for path: {}", filePath);

        // URL oficial da API de Storage do Supabase para criar links assinados
        String supabaseEndpoint = supabaseUrl + "/storage/v1/object/upload/sign/" + bucketName + "/" + filePath;

        try {
            // Corpo da requisição exigido pelo Supabase (tempo de expiração em segundos)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("expiresIn", 900); // URL válida por 15 minutos

            // Faz a chamada síncrona para o Supabase injetando as chaves de segurança
            Map<String, Object> supabaseResponse = restClient.post()
                    .uri(supabaseEndpoint)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .header("apikey", supabaseServiceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (supabaseResponse == null || !supabaseResponse.containsKey("url")) {
                throw new IllegalStateException("Supabase returned an empty or invalid response.");
            }

            // O Supabase retorna um caminho relativo em "url". Montamos a URL absoluta:
            // Ex: /object/upload/sign/assets-neoframe/...path...?token=real_token
            String relativeUploadUrl = (String) supabaseResponse.get("url");
            String realUploadUrl = supabaseUrl + "/storage/v1" + relativeUploadUrl;

            String finalUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;

            log.info("Successfully generated real authenticated Supabase upload URL.");

            Map<String, String> urls = new HashMap<>();
            urls.put("uploadUrl", realUploadUrl);
            urls.put("finalUrl", finalUrl);

            return urls;

        } catch (Exception e) {
            log.error("Critical error while requesting pre-signed URL from Supabase REST API: ", e);
            throw new RuntimeException("Falha ao gerar credenciais de upload com o provedor de storage.");
        }
    }
}