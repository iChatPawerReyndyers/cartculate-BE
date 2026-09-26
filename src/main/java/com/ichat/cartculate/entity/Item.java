package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Master item list. e.g. "Carrots" (category "Vegetables"). */
@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** e.g. Produce, Meat, Household. Used by the Insights category pie chart. */
    @Column(nullable = false)
    private String category;

    /**
     * How this item is typically sold/measured, e.g. "kg", "pack", "pc".
     * Nullable - a null unit just means the item's name is shown bare with
     * no "(unit)" suffix. Feeds Feature 1's Pricing Format rule
     * (itemName (unit) e.g. "Carrots (kg)", "Napkin (pack)") on the
     * frontend's CartItem display.
     */
    @Column(nullable = true)
    private String unit;

    /**
     * True if this item should appear as an option in the Recipe modal's
     * ingredient picker. Most catalog items (toiletries, snacks, etc.)
     * aren't recipe ingredients at all, so this keeps that picker from
     * being cluttered with everything in the catalog - only items
     * explicitly flagged "can be an ingredient" (via the Add/Edit Product
     * form) show up there.
     */
    @Column(nullable = false)
    private boolean isIngredient = false;

    /**
     * Feature: "checkbox per item on Pricing tab controls what shows in
     * Cart tab". Defaults true so every item already in the catalog
     * before this feature existed keeps behaving exactly as before
     * (visible in Cart) unless someone explicitly unchecks it.
     * columnDefinition (not just nullable=false) matters here: ddl-auto=
     * update running this ALTER TABLE against an ALREADY-POPULATED items
     * table needs a real DB-level DEFAULT to backfill existing rows,
     * same reasoning as the nullable columns added on User.java - a bare
     * "not null" with no default fails against existing data in Postgres.
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean includeInCart = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_store_id", nullable = true)
    private Store defaultStore;

    /**
     * Alternate unit for costing recipes ONLY - never shown as the item's
     * main unit, never changes its price, and is completely separate from
     * ItemService.convertUnit's one-off "switch the item's whole unit and
     * rewrite everything" conversion.
     *
     * Example: Carrots is priced per "pc" (unit = "pc"). A recipe might
     * still record "0.5 kg carrots" (altUnit = "kg", altUnitQuantity =
     * 0.10, meaning 1 pc ≈ 0.10 kg on average). RecipeService resolves that
     * line's cost as (0.5 kg / 0.10 kg-per-pc) x 5 pc-equivalent x the
     * per-pc price - see RecipeService.toIngredientDto() and the
     * frontend's recipeLogic.ts::priceableQuantity(), which do the actual
     * math. Null/null means no alternate unit is configured for this item;
     * a recipe line in any unit other than the item's own `unit` is then
     * left unconverted, same as before this feature existed.
     */
    @Column(nullable = true)
    private String altUnit;

    /** How many of altUnit equal ONE of this item's own `unit` (e.g. 0.10 kg per 1 pc). Null when altUnit is null. */
    @Column(nullable = true, precision = 10, scale = 4)
    private BigDecimal altUnitQuantity;
}