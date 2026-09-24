package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * A product that's automatically added as an ingredient row whenever THIS
 * USER creates a new recipe (e.g. salt, garlic, onion - things almost
 * every recipe uses). Per-user, matching cart/recipes/category defaults -
 * each user keeps their own list.
 *
 * Only affects the moment a recipe is created - the frontend seeds a new
 * recipe form's initial rows from this list (NewRecipeModal, mode='add').
 * Editing an existing recipe never adds or removes these automatically, and
 * removing/editing one of these rows on a specific recipe only affects that
 * recipe, not this list.
 */
@Entity
@Table(name = "default_ingredients", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public DefaultIngredient(User user, Item item) {
        this.user = user;
        this.item = item;
        this.createdAt = Instant.now();
    }
}
