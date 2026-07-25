package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for PUT /api/stores/{storeId}/prices.
 * Field names deliberately match the frontend's buildStorePriceUpdates()
 * return shape ({ itemId, storeId, priceAmount }) - storeId is redundant
 * per-item here since it's already in the URL path, but kept for a 1:1
 * match with what the frontend already builds, avoiding a mapping step.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStorePricesRequest {
    private List<PriceUpdate> updates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceUpdate {
        private Long itemId;
        private BigDecimal priceAmount;
    }
}
