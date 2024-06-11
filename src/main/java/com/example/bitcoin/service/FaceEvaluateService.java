package com.example.bitcoin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FaceEvaluateService {

    @Autowired
    private OpenAIService openAIService;

    public List<String> evaluateImages(List<MultipartFile> images) throws IOException {
        return images.stream().map(image -> {
            try {
                byte[] imageData = image.getBytes();
                return openAIService.processRequestWithImage(imageData);
            } catch (IOException e) {
                log.error("Error processing image", e);
                return "Error: " + e.getMessage();
            }
        }).collect(Collectors.toList());
    }
}