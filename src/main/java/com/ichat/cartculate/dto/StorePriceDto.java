package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorePriceDto {
    private String itemId;
    private String itemName;
    private String storeId;
    private String storeName;
    private BigDecimal priceAmount;
    /** "SCAN" or "MANUAL" - see StorePrice.java's priceSource field. */
    private String priceSource;
    /**
     * True if this price is the requesting user's own personal override
     * (see UserStorePrice.java) rather than the shared baseline everyone
     * else sees. Only meaningful on responses from a user-scoped endpoint
     * (GET /api/users/{userId}/store-prices) - always false on the
     * unscoped /api/store-prices / /api/stores/{storeId}/prices
     * endpoints, since those only ever return the shared baseline.
     */
    private boolean isPersonalOverride;
}