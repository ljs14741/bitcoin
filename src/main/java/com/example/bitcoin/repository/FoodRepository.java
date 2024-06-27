package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Food;
import com.example.bitcoin.entity.FoodCategory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByCategory(FoodCategory category);

    @Query("SELECT f FROM food f WHERE f.foodType = :foodType OR f.foodType = :combinedType ORDER BY RAND()")
    List<Food> findRandomFoodByType(@Param("foodType") Food.FoodType foodType, @Param("combinedType") Food.FoodType combinedType, PageRequest pageRequest);
}