package com.example.bitcoin.service;

import java.util.List;
import java.util.Random;

import com.example.bitcoin.dto.FoodDTO;
import com.example.bitcoin.entity.Food;
import com.example.bitcoin.entity.FoodCategory;
import com.example.bitcoin.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoodService {
    @Autowired
    private FoodRepository foodRepository;

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    public List<Food> getFoodsByCategory(FoodCategory category) {
        return foodRepository.findByCategory(category);
    }

    public Food getRandomFood() {
        List<Food> foods = foodRepository.findAll();
        Random random = new Random();
        return foods.get(random.nextInt(foods.size()));
    }

    public Food getRandomFoodByCategory(FoodCategory category) {
        List<Food> foods = foodRepository.findByCategory(category);
        Random random = new Random();
        return foods.get(random.nextInt(foods.size()));
    }

    public void saveFood(Food food) {
        foodRepository.save(food);
    }

    public FoodDTO convertToDTO(Food food) {
        return FoodDTO.builder()
                .id(food.getId())
                .foodName(food.getFoodName())
                .foodImg(food.getFoodImg())
                .build();
    }
}