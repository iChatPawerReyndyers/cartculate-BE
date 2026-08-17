package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for POST /api/items - adding a new product to the master catalog. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {
    private String name;
    private String category;
    /** e.g. "kg", "pack", "pc" - optional, null if the item has no natural unit label. */
    private String unit;
    /** True if this item should appear in the Recipe modal's ingredient picker. */
    private boolean isIngredient;
}