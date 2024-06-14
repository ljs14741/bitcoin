package com.example.bitcoin.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Service
@Slf4j
public class FaceApiService {

    public String callFlaskApi() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // 로컬 이미지 파일 경로 설정
        Path imagePath = Paths.get("src/main/resources/static/img/testImg.png");

        log.info("111: " + imagePath);

        // Multipart body 생성
        String boundary = "----WebKitFormBoundary" + new Random().nextInt();
        BodyPublisher bodyPublisher = ofMimeMultipartData(imagePath, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/predict"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static BodyPublisher ofMimeMultipartData(Path imagePath, String boundary) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();

        byteArrays.add(("--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + imagePath.getFileName() + "\"\r\n" +
                "Content-Type: image/png\r\n\r\n").getBytes());
        byteArrays.add(Files.readAllBytes(imagePath));
        byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes());

        return BodyPublishers.ofByteArrays(byteArrays);
    }
}