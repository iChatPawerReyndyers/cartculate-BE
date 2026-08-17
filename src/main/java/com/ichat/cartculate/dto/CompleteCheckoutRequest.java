package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Request body for POST /api/users/{userId}/cart/complete-checkout. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteCheckoutRequest {
    private Long storeId;

    /**
     * How much of each checked-off item was ACTUALLY bought (keyed by
     * itemId, not cart row id - one item can span multiple rows if it's
     * sourced from more than one recipe). Any checked item not present
     * here is treated as fully bought, for backward compatibility.
     * Defaults to the full needed amount in the frontend's reconciliation
     * modal, but is user-adjustable down for partial purchases.
     */
    private List<BoughtItemInput> boughtItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoughtItemInput {
        private Long itemId;
        private BigDecimal quantityBought;
    }
}
