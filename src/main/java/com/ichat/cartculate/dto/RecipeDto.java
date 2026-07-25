package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/** Mirrors the frontend's Recipe interface (src/types/index.ts). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private String id;
    private String name;
    private List<RecipeIngredientDto> ingredients;
}
