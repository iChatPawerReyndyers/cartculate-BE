package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Mirrors the frontend's CartRow interface (src/types/index.ts) field-for-field,
 * so GET /api/users/{userId}/cart can be dropped straight into cartLogic.consolidateCart().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartRowDto {
    private String id;
    private String itemId;
    private String itemName;
    private String category;
    /** e.g. "kg", "pack", "pc" - feeds Feature 1's Pricing Format rule ("itemName (unit)") on the frontend. */
    private String unit;
    private String storeId;
    private String storeName;
    private BigDecimal price;
    private BigDecimal quantity;
    private String sourceRecipeId;
    private String sourceRecipeName;
    /** Aggregate amount already available at home, subtracted from the "need to buy" total. */
    private BigDecimal overridePantryQty;
    /** Free-text/emoji tag context for the pantry override, e.g. "Freezer Find". */
    private String overrideReason;
    /**
     * Checkbox state during "Start Grocery" (Away Mode) trip mode.
     * BUGFIX: same pre-existing Jackson "is"-prefix-stripping bug as
     * ItemDto.isIngredient - see that file's comment. Without
     * @JsonProperty, this serialized as JSON key "checkedCheckout" instead
     * of "isCheckedCheckout", so the frontend's CartRow type never
     * actually received this field - every checkout checkbox silently
     * came back unchecked on every cart load, regardless of real state.
     */
    @JsonProperty("isCheckedCheckout")
    private boolean isCheckedCheckout;
}
