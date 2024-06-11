package com.example.bitcoin.controller;

import com.example.bitcoin.service.FaceEvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
public class FaceEvaluateController {

    @Autowired
    private FaceEvaluateService faceEvaluateService;

    @GetMapping("/faceEvaluate")
    public String faceEvaluate() {
        return "faceEvaluate";
    }

    @PostMapping("/api/evaluate")
    @ResponseBody
    public EvaluationResponse evaluateImages(@RequestParam("images") List<MultipartFile> images) {
        try {
            List<String> evaluations = faceEvaluateService.evaluateImages(images);
            return new EvaluationResponse(evaluations);
        } catch (IOException e) {
            e.printStackTrace();
            return new EvaluationResponse(Collections.singletonList("Error: " + e.getMessage()));
        }
    }

    static class EvaluationResponse {
        private List<String> results;

        public EvaluationResponse(List<String> results) {
            this.results = results;
        }

        public List<String> getResults() {
            return results;
        }

        public void setResults(List<String> results) {
            this.results = results;
        }
    }
}