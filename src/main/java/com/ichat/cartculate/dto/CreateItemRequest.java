package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for POST /api/items - adding a new product to the master catalog. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {
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
    /** Explicit default store; null uses the category default when available. */
    private Long defaultStoreId;
    /** Optional alternate unit for costing recipes only, e.g. "kg" for a "pc"-priced item. See Item.java. */
    private String altUnit;
    /** How many of altUnit equal ONE of this item's own unit. Required (and validated) whenever altUnit is set. */
    private java.math.BigDecimal altUnitQuantity;
}