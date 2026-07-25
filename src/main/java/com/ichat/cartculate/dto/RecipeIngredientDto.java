package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Mirrors the frontend's RecipeIngredient interface. Note: defaultStoreId/
 * defaultStoreName/defaultPrice aren't columns on Recipe_Ingredient itself
 * (per the ER model, that table only has recipe_id/item_id/base_quantity) -
 * RecipeService derives them by picking the cheapest known Store_Price for
 * that item. See the TODO in RecipeService for how to make this configurable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDto {
    private String itemId;
    private String itemName;
    private BigDecimal baseQuantity;
    private String unit;
    private String defaultStoreId;
    private String defaultStoreName;
    private BigDecimal defaultPrice;
}
