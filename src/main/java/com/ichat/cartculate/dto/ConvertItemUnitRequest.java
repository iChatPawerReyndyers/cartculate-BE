package com.ichat.cartculate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * POST /api/items/{itemId}/convert-unit body.
 *
 * Example: a product priced per "kg" is switched to "pc" and the user says
 * there are 8 pc in 1 kg -> newUnit = "pc", factor = 8.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertItemUnitRequest {
    /** The unit the product switches to. Blank/null means "no unit" (a plain count). */
    private String newUnit;
    /** How many of newUnit make up ONE of the product's current unit. Must be > 0. */
    private BigDecimal factor;
}
