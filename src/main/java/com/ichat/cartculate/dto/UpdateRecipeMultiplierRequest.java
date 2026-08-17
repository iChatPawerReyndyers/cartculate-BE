package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** Request body for PATCH /api/users/{userId}/recipes/{recipeId}/multiplier. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeMultiplierRequest {
    private BigDecimal multiplier;
}