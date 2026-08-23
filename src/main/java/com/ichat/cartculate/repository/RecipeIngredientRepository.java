package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeId(Long recipeId);

    /** Used when deleting a product entirely - every recipe ingredient line (across all recipes) referencing that item. */
    List<RecipeIngredient> findByItem_Id(Long itemId);
}
