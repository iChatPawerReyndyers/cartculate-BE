package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Scaler tracked directly on the recipe card (e.g. "x2" via +/- buttons),
     * per Feature 3. Fractional values (x0.5, x1.5) are allowed. Changing
     * this now directly drives the corresponding user_cart_item rows - see
     * RecipeService.updateMultiplier() - rather than requiring a separate
     * "Add to cart" action.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentMultiplier = BigDecimal.ZERO;
}