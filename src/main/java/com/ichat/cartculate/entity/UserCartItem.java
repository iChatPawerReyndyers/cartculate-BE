package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * One row of a user's active cart (the "Inventory Mitigation Engine" table
 * from the updated spec). Nullable sourceRecipe: if null, this quantity was
 * added manually via +/- on the main screen and defaults to the "Others"
 * bucket in the item's accordion breakdown.
 *
 * NOTE ON PK: the spec's SQL defines the primary key as the composite
 * (user_id, item_id, store_id, source_recipe_id) - but Postgres requires
 * every PK column to be NOT NULL, and source_recipe_id is nullable by
 * design (NULL = the "Others" bucket). A surrogate `id` PK is used instead,
 * with the intended business key enforced as a unique constraint below.
 * Postgres treats multiple NULLs as distinct in a unique index, so this
 * preserves one-row-per-(item,store,recipe) semantics without rejecting
 * "Others" rows the way a literal NULL-inclusive PRIMARY KEY would.
 */
@Entity
@Table(
        name = "user_cart_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_cart_item_business_key",
                columnNames = {"user_id", "item_id", "store_id", "source_recipe_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Nullable. If null, item defaults to the "Others" bucket in the UI. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_recipe_id", nullable = true)
    private Recipe sourceRecipe;

    /** Calculated recipe-active quantity to buy (or manually-set "Others" quantity). */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /** Aggregate amount already available at home, subtracted from the "need to buy" total. */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal overridePantryQty = BigDecimal.ZERO;

    /** Free-text/emoji tag context for the pantry override, e.g. "🧊 Freezer Find", "Pantry Stock". */
    @Column(nullable = true)
    private String overrideReason;

    /** Checkbox state during "Start Grocery" (Away Mode) trip mode. */
    @Column(nullable = false)
    private Boolean isCheckedCheckout = false;
}