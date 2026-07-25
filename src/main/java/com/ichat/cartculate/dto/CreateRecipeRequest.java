package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Request body for POST /api/users/{userId}/recipes - creating a new recipe. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeRequest {
    private String name;
    private List<IngredientInput> ingredients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientInput {
        private Long itemId;
        private BigDecimal baseQuantity;
        private String unit;
    }
}
