package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Mirrors one archived RECEIPT (not one line item, per the updated spec's
 * purchase_history table). itemManifestJson is the raw JSON string from the
 * entity - the frontend parses it for display; see PurchaseHistory.java for
 * the expected array shape.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecordDto {
    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private BigDecimal totalReceiptSpent;
    private String purchaseDate; // ISO date string
    private String itemManifestJson;
}