package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Mirrors the frontend's RecipeIngredient interface. defaultStoreId/
 * defaultStoreName/defaultPrice come from the ingredient's targetStoreId
 * (custom routing, per the updated spec) when set, otherwise RecipeService
 * falls back to whichever store has the cheapest known Store_Price for
 * that item. See RecipeService.toIngredientDto() for the fallback logic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDto {
    private String itemId;
    private String itemName;
    private String category;
    private BigDecimal baseQuantity;
    private String unit;
    private String defaultStoreId;
    private String defaultStoreName;
    private BigDecimal defaultPrice;
    /** True if defaultStoreId came from an explicit targetStoreId rather than the cheapest-price fallback. */
    private boolean isCustomRouted;
    /** True if this ingredient is optional (garnish, skippable spice, etc.) - see RecipeIngredient.java. */
    private boolean isOptional;
}
