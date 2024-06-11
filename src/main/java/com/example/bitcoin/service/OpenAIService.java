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

        // Step 1: Create the chat completion request
        String chatResponse = createChatCompletion();
        log.info("Chat Completion Response: {}", chatResponse);

        // Step 2: Upload the image
        String imageUploadResponse = uploadImage(imageData);
        log.info("Image Upload Response: {}", imageUploadResponse);

        // Step 3: Generate text response based on image upload
        String evaluationResponse = generateEvaluationText();
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

    private String createChatCompletion() throws IOException {
        Map<String, Object> userMessage1 = new HashMap<>();
        userMessage1.put("role", "user");
        userMessage1.put("content", "안녕 너는 이번에 '웃음 콘테스트'에 얼굴 평가 심사위원이야. 이 콘테스트는 사람들과 즐겁게 즐기며 웃기 위해 모인 자리야. 그래서 너는 심사에 진지하게 임할 필요가 없어. 예를 들어 사람들이 인물 사진을 업로드하면 '당신은 패션은 모나미룩인가요? 센스가 꽝이군요. 5.0점 드리겠습니다. ^^', '첫번째 사진의 검은색 상의는 다리 밑에서 주워 오신 건가요? 너덜너덜하군요. 3.3점 드리겠습니다. ^^', 'Unbelievable! 두번째 사진분은 미스코리아 출신인가요? 10.0점 드리겠습니다. ^^' 이런 식으로 답변하면 돼. 많은 사람들이 참여하기 때문에 재미를 위해서는 다양한 답변을 하면 돼~ 그리고 최저점수는 1.0점 최고점수는 10.0점이야. 점수는 소수점 첫째 자리까지 나타내면 돼.");

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4");
        payload.put("messages", new Object[]{userMessage1});

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

    private String generateEvaluationText() throws IOException {
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "방금 업로드된 이미지를 평가해줘.");

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