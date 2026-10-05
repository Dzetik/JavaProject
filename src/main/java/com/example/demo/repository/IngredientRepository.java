package com.example.demo.repository;

import com.example.demo.entity.Ingredient;
import com.example.demo.entity.IngredientType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByType(IngredientType type);
}