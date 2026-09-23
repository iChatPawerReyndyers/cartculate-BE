package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Per-USER category settings applied when creating a NEW product in that
 * category - never retroactively changes an existing item, and an item's
 * own explicit value always takes priority once set (see
 * ItemService.createItem / ProductModal.tsx).
 *
 * Replaces the old app-wide CategoryDefault table: each user now has their
 * own defaults (matching how cart, recipes, and default ingredients already
 * work per-user), rather than one user's setting affecting everyone. The
 * old CategoryDefault entity/table is left in place, unused, as a record of
 * what existed before the switch (see the data.sql migration block
 * "category-defaults-per-user-v1", which copies the old shared rows into
 * this table for the users who already had them).
 */
@Entity
@Table(name = "user_category_defaults", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Free-text category name, e.g. "Vegetables" - not a fixed enum. */
    @Column(nullable = false)
    private String category;

    /** Nullable - "no default store set" for this category. */
    @ManyToOne
    @JoinColumn(name = "default_store_id")
    private Store defaultStore;

    /** Whether a new product created in this category starts with its "Ingredient" toggle on. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean defaultIsIngredient = false;

    public UserCategoryDefault(User user, String category, Store defaultStore, boolean defaultIsIngredient) {
        this.user = user;
        this.category = category;
        this.defaultStore = defaultStore;
        this.defaultIsIngredient = defaultIsIngredient;
    }
}