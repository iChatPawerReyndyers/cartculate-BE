package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** Mirrors the frontend's PurchaseRecord interface (src/types/index.ts). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecordDto {
    private String id;
    private String userId;
    private String itemId;
    private String itemName;
    private String category;
    private String storeId;
    private String storeName;
    private BigDecimal quantityBought;
    private BigDecimal pricePerUnit;
    private String purchaseDate; // ISO date string
}
