package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** One ingredient line on a saved recipe, at base (x1) quantity. */
@Entity
@Table(name = "recipe_ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseQuantity;

    /** Nullable. e.g. "g" for "500g beef cubes"; null for countable items like "2 carrots". */
    @Column(nullable = true)
    private String unit;
}
