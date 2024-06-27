package com.example.bitcoin.controller;

import com.example.bitcoin.dto.FoodCategoryDTO;
import com.example.bitcoin.dto.FoodDTO;
import com.example.bitcoin.entity.Food;
import com.example.bitcoin.entity.FoodCategory;
import com.example.bitcoin.service.FoodCategoryService;
import com.example.bitcoin.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FoodController {
    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    private FoodService foodService;

    @GetMapping("/food")
    public String foodMain(Model model) {
        List<FoodCategoryDTO> categories = foodCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "food";
    }

    @GetMapping("/food/randomCategory")
    @ResponseBody
    public FoodCategoryDTO getRandomCategory() {
        return foodCategoryService.getRandomCategoryDTO();
    }

    @GetMapping("/food/randomFood")
    @ResponseBody
    public FoodDTO getRandomFood(@RequestParam(required = false) Long categoryId) {
        Food randomFood;
        if (categoryId != null) {
            FoodCategory category = foodCategoryService.getCategoryById(categoryId);
            randomFood = foodService.getRandomFoodByCategory(category);
        } else {
            randomFood = foodService.getRandomFood();
        }
        return foodService.convertToDTO(randomFood);
    }

    @GetMapping("/food/randomLunch")
    @ResponseBody
    public FoodDTO getRandomLunch() {
        Food randomFood = foodService.getRandomFoodByType(Food.FoodType.점심);
        return foodService.convertToDTO(randomFood);
    }

    @GetMapping("/food/randomSnacks")
    @ResponseBody
    public FoodDTO getRandomSnacks() {
        Food randomFood = foodService.getRandomFoodByType(Food.FoodType.술안주);
        return foodService.convertToDTO(randomFood);
    }

    @GetMapping("/food/{categoryId}")
    public String getFoodByCategory(@PathVariable Long categoryId, Model model) {
        FoodCategory category = foodCategoryService.getCategoryById(categoryId);
        List<Food> foods = foodService.getFoodsByCategory(category);
        model.addAttribute("foods", foods);
        return "foodDetail";
    }

    @PostMapping("/food")
    public String addFood(@RequestParam String foodName, @RequestParam Long categoryId) {
        FoodCategory category = foodCategoryService.getCategoryById(categoryId);
        Food food = new Food();
        food.setFoodName(foodName);
        food.setCategory(category);
        foodService.saveFood(food);
        return "redirect:/food";
    }
}