package com.example.bitcoin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenAIService {

    private final String apiKey = System.getenv("OPENAI_API_KEY");
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String processRequestWithImage(byte[] imageData) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("OpenAI API key is not set. Please set the OPENAI_API_KEY environment variable.");
        }

        // Step 1: Upload the image
        String imageUploadResponse = uploadImage(imageData);
        log.info("Image Upload Response: {}", imageUploadResponse);

        // Parse the image upload response to get the file ID
        JsonNode imageResponseJson = objectMapper.readTree(imageUploadResponse);
        String imageFileId = imageResponseJson.path("id").asText();
        if (imageFileId == null || imageFileId.isEmpty()) {
            throw new IOException("Image upload failed, file ID is missing");
        }

        // Step 2: Request evaluation based on image ID
        String evaluationResponse = requestImageEvaluation(imageFileId);
        log.info("Evaluation Response: {}", evaluationResponse);

        // Parse the response to extract the content
        JsonNode jsonResponse = objectMapper.readTree(evaluationResponse);
        JsonNode choicesNode = jsonResponse.path("choices");
        if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.size() == 0) {
            throw new IOException("Unexpected response format: choices node is missing or empty");
        }

        String content = choicesNode.get(0).path("message").path("content").asText();
        return content;
    }

    private String uploadImage(byte[] imageData) throws IOException {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.png",
                        RequestBody.create(imageData, MediaType.parse("image/png")))
                .addFormDataPart("purpose", "fine-tune");

        RequestBody body = bodyBuilder.build();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/files")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ": " + response.body().string());
            }

            return response.body().string();
        }
    }

    private String requestImageEvaluation(String imageFileId) throws IOException {
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "방금 업로드된 이미지 파일 ID: " + imageFileId + " 를 평가해줘.");

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4");
        payload.put("messages", new Object[]{userMessage});

        String jsonPayload = objectMapper.writeValueAsString(payload);

        RequestBody requestBody = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ": " + response.body().string());
            }
            return response.body().string();
        }
    }
}