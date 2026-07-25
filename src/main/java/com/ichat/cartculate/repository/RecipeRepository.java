package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserId(Long userId);
}
