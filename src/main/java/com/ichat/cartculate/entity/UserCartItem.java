package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * One row of a user's active cart. Nullable sourceRecipe: if null, this
 * quantity was added manually via +/- on the main screen and defaults to
 * the "Others" bucket in the item's accordion breakdown.
 */
@Entity
@Table(name = "user_cart_items")
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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    /** Nullable. If null, item defaults to the "Others" bucket in the UI. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_recipe_id", nullable = true)
    private Recipe sourceRecipe;
}
