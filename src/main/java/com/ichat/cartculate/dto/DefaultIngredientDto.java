package com.ichat.cartculate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One entry in the "Default ingredients" list (Pricing tab). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultIngredientDto {
    private String itemId;
    private String itemName;
    private String category;
    private String unit;
}