package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    /**
     * True if defaultStoreId came from an explicit targetStoreId rather
     * than the cheapest-price fallback.
     * BUGFIX: same pre-existing Jackson "is"-prefix-stripping bug as
     * ItemDto.isIngredient - see that file's comment. Both this field and
     * isOptional below serialized without their "is" prefix, so the
     * frontend never actually received either - the custom-routing
     * indicator and the optional-ingredient badge were silently always
     * false on every recipe, regardless of real data.
     */
    @JsonProperty("isCustomRouted")
    private boolean isCustomRouted;
    /** True if this ingredient is optional (garnish, skippable spice, etc.) - see RecipeIngredient.java. */
    @JsonProperty("isOptional")
    private boolean isOptional;
    /** True when recipe scaling should sync this ingredient to the cart. */
    @JsonProperty("addToCart")
    private boolean addToCart;
}
