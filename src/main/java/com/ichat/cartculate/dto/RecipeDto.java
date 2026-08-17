package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Mirrors the frontend's Recipe interface (src/types/index.ts). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private String id;
    private String name;
    private List<RecipeIngredientDto> ingredients;
    /** Scaler tracked directly on the recipe card (e.g. "x2" via +/- buttons). Fractional values allowed. */
    private BigDecimal currentMultiplier;
}