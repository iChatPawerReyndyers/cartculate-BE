package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * One ingredient line on a saved recipe, at base (x1) quantity.
 * Composite PK (recipe_id, item_id) per the updated spec - a recipe can't
 * list the same item twice.
 */
@Entity
@Table(name = "recipe_ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @EmbeddedId
    private RecipeIngredientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeId")
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal baseQuantity;

    /** Nullable. e.g. "g" for "500g beef cubes"; null for countable items like "2 carrots". */
    @Column(nullable = true)
    private String unit;

    /**
     * Custom store routing for this ingredient (per updated spec's Store & Route feature).
     * Null = fall back to whichever store has the lowest known price for this item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_store_id", nullable = true)
    private Store targetStore;

    /**
     * True if this ingredient is a "nice to have, not essential" addition
     * to the recipe (e.g. garnish, a spice someone might skip). Optional
     * ingredients still get scaled and priced normally, but are visually
     * flagged in the UI and are excluded from Feature 2's "recipe requires
     * this much" floor - buying less of an optional ingredient than the
     * recipe calls for shouldn't trigger the Pantry Treasure Found
     * interception the way a core ingredient running short would.
     */
    @Column(nullable = false)
    private boolean isOptional = false;
}
