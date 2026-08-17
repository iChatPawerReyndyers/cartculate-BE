package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String id;
    private String name;
    private String category;
    /** e.g. "kg", "pack", "pc" - null means no unit suffix shown. See Item.java. */
    private String unit;
    /** True if this item can be picked as a recipe ingredient. See Item.java. */
    private boolean isIngredient;
}