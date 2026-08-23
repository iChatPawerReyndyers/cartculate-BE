package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String id;
    private String name;
    private String category;
    /** e.g. "kg", "pack", "pc" - null means no unit suffix shown. See Item.java. */
    private String unit;
    /**
     * True if this item can be picked as a recipe ingredient. See Item.java.
     *
     * BUGFIX: pre-existing bug, unrelated to any feature work in this
     * session - explicit @JsonProperty is required here. Without it,
     * Jackson's default JavaBean naming convention strips the "is" prefix
     * from the Lombok-generated isIngredient() getter and serializes this
     * as JSON key "ingredient", not "isIngredient". The frontend's
     * ItemResponse type expects "isIngredient" and silently got undefined
     * (falsy) for every item, so NewRecipeModal's ingredientItems filter
     * was ALWAYS empty regardless of actual seeded data - the real root
     * cause of "+ Add ingredient" being unclickable (it wasn't a UI/touch
     * bug, it was silently doing nothing because there was nothing to add).
     */
    @JsonProperty("isIngredient")
    private boolean isIngredient;
    /** True if this item should appear in the Cart tab at all. See Item.java. */
    private boolean includeInCart;
}