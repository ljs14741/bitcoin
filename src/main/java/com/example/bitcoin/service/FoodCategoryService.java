package com.example.bitcoin.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.example.bitcoin.dto.FoodCategoryDTO;
import com.example.bitcoin.entity.FoodCategory;
import com.example.bitcoin.repository.FoodCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoodCategoryService {
    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    public List<FoodCategoryDTO> getAllCategories() {
        return foodCategoryRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public FoodCategory getCategoryById(Long id) {
        return foodCategoryRepository.findById(id).orElse(null);
    }

    public FoodCategoryDTO getRandomCategoryDTO() {
        List<FoodCategory> categories = foodCategoryRepository.findAll();
        FoodCategory randomCategory = categories.get(new Random().nextInt(categories.size()));
        return convertToDTO(randomCategory);
    }

    private FoodCategoryDTO convertToDTO(FoodCategory category) {
        return FoodCategoryDTO.builder()
                .id(category.getId())
                .foodCategoryName(category.getFoodCategoryName())
                .foodCategoryImg(category.getFoodCategoryImg())
                .createdDate(category.getCreatedDate())
                .updatedDate(category.getUpdatedDate())
                .build();
    }
}