package com.example.bitcoin.controller;

import com.example.bitcoin.service.FaceApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class FaceApiController {

    @Autowired
    private FaceApiService faceApiService;

    @GetMapping("/testUpload")
    public ResponseEntity<String> uploadFile() {
        try {
            String response = faceApiService.callFlaskApi();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

