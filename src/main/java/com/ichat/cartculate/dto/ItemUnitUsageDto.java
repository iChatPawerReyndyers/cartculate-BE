package com.ichat.cartculate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * GET /api/items/{itemId}/unit-usage response: everything that would be
 * touched if this product's unit were converted. The app shows it in the
 * "Convert unit" prompt so the user can see the effect before confirming.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUnitUsageDto {

    /** Every recipe ingredient line that uses this product (across all recipes). */
    private List<RecipeLine> recipeLines;
    /** Shared prices, one per store. (Personal price overrides are converted too but not listed.) */
    private List<PriceLine> prices;
    /** Number of cart rows (all users) holding this product. */
    private int cartRows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeLine {
        private String recipeName;
        private BigDecimal quantity;
        private String unit;
        /**
         * The line's quantity expressed in the product's CURRENT unit (e.g.
         * 500 g -> 0.5 for a product measured in kg). Null when the line's
         * unit can't be related to the product's unit (e.g. "pack" vs "kg"),
         * in which case the conversion leaves that line untouched.
         */
        private BigDecimal quantityInItemUnit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceLine {
        private String storeName;
        private BigDecimal priceAmount;
    }
}
