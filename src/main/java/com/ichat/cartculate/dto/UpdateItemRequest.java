package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PUT /api/items/{itemId} - editing an existing product's name/category/unit. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    private String name;
    private String category;
    /** e.g. "kg", "pack", "pc" - optional, null if the item has no natural unit label. */
    private String unit;
    /** True if this item should appear in the Recipe modal's ingredient picker. */
    private boolean isIngredient;
}