package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Request body for POST /api/users/{userId}/purchases - bulk insert. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchasesRequest {
    private List<PurchaseInput> purchases;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseInput {
        private Long itemId;
        private Long storeId;
        private BigDecimal quantityBought;
        private BigDecimal pricePerUnit;
        private String purchaseDate; // ISO date string, optional - defaults to now
    }
}
