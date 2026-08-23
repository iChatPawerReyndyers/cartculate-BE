package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Per-category settings applied when creating a NEW product in that
 * category - never retroactively changes an existing item, and an item's
 * own explicit value always takes priority over these once set (see
 * ItemService.createItem / the frontend's ProductModal).
 *
 * Genuinely new on the backend - the frontend's categoryDefaultStoreApi.ts
 * already had a client for "default store per category" (via
 * withMockFallback's mock DB), but there was no real server-side
 * implementation at all until now; this entity backs both that existing
 * default-store feature and the new default-ingredient-flag feature in
 * one table, since they're both just "settings for this category".
 */
@Entity
@Table(name = "category_defaults")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDefault {

    /** The category name itself, e.g. "Produce" - categories are free-text, not a fixed enum, so this is the natural key. */
    @Id
    private String category;

    /** Nullable - "no default store set" for this category. */
    @ManyToOne
    @JoinColumn(name = "default_store_id")
    private Store defaultStore;

    /** Whether a new product created in this category starts with its "Ingredient" toggle on. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean defaultIsIngredient = false;
}
