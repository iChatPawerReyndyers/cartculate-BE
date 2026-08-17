package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/** Request body for PUT /api/users/{userId}/recipes/{recipeId} - editing an existing recipe. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeRequest {
    private String name;
    private List<CreateRecipeRequest.IngredientInput> ingredients;
}