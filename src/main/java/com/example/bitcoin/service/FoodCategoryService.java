package com.example.bitcoin.service;

import java.util.List;
import java.util.Random;

import com.example.bitcoin.entity.FoodCategory;
import com.example.bitcoin.repository.FoodCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoodCategoryService {
    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAll();
    }

    public FoodCategory getRandomCategory() {
        List<FoodCategory> categories = foodCategoryRepository.findAll();
        Random random = new Random();
        return categories.get(random.nextInt(categories.size()));
    }

    public FoodCategory getCategoryById(Long id) {
        return foodCategoryRepository.findById(id).orElse(null);
    }
}