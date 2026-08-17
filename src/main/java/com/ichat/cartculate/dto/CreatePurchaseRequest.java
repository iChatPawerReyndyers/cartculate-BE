package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for POST /api/users/{userId}/purchases - archives ONE
 * receipt (Checkout completion), per the updated spec. The structured
 * `items` list here is what the frontend already has in memory (checked
 * cart rows); the service serializes it into itemManifestJson server-side
 * so callers don't have to hand-build JSON strings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseRequest {
    private Long storeId;
    private BigDecimal totalReceiptSpent;
    private String purchaseDate; // ISO date string, optional - defaults to now

    private List<ManifestItemInput> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManifestItemInput {
        private Long itemId;
        private String itemName;
        private String category;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
    }
}