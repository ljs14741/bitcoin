package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Food;
import com.example.bitcoin.entity.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByCategory(FoodCategory category);
}