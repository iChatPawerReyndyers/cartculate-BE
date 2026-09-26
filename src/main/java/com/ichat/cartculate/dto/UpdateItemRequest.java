package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for PUT /api/items/{itemId} - editing an existing product's name/category/unit. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    private String name;
    private String category;
    /** e.g. "kg", "pack", "pc" - optional, null if the item has no natural unit label. */
    private String unit;
    /**
     * True if this item should appear in the Recipe modal's ingredient picker.
     *
     * BUGFIX: explicit @JsonProperty is required. Lombok generates isIngredient()
     * for a boolean field named "isIngredient", and Jackson strips the "is"
     * prefix, so it was reading/writing the JSON key "ingredient" instead of
     * "isIngredient" (what the frontend sends). The value the app sent was
     * silently dropped, so every product saved through the API ended up with
     * isIngredient=false. Same fix as ItemDto.
     */
    @JsonProperty("isIngredient")
    private boolean isIngredient;
    /** Explicit default store, or null to clear it. */
    private Long defaultStoreId;
    /** Optional alternate unit for costing recipes only, e.g. "kg" for a "pc"-priced item. Null clears it. See Item.java. */
    private String altUnit;
    /** How many of altUnit equal ONE of this item's own unit. Required (and validated) whenever altUnit is set. */
    private java.math.BigDecimal altUnitQuantity;
}