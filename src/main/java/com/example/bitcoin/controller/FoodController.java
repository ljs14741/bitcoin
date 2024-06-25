package com.example.bitcoin.controller;

import com.example.bitcoin.entity.Food;
import com.example.bitcoin.entity.FoodCategory;
import com.example.bitcoin.service.FoodCategoryService;
import com.example.bitcoin.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FoodController {
    @Autowired
    private FoodCategoryService foodCategoryService;

    @Autowired
    private FoodService foodService;

    @GetMapping("/food")
    public String foodMain(Model model) {
        List<FoodCategory> categories = foodCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "food";
    }

    @GetMapping("/food/randomCategory")
    public String getRandomCategory(Model model) {
        FoodCategory randomCategory = foodCategoryService.getRandomCategory();
        model.addAttribute("randomCategory", randomCategory);
        return "food";
    }

    @GetMapping("/food/randomFood")
    public String getRandomFood(Model model, @RequestParam(required = false) Long categoryId) {
        Food randomFood;
        if (categoryId != null) {
            FoodCategory category = foodCategoryService.getCategoryById(categoryId);
            randomFood = foodService.getRandomFoodByCategory(category);
        } else {
            randomFood = foodService.getRandomFood();
        }
        model.addAttribute("randomFood", randomFood);
        return "food";
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