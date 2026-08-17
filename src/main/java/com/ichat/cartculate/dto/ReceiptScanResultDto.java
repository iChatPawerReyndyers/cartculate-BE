package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Full response from scanning one receipt image. Mirrors the frontend's
 * ReceiptScanResult type field-for-field, so it can be dropped straight into
 * ReceiptScannerScreen.tsx's existing review/confirm flow with no reshaping.
 *
 * This is a PREVIEW only - nothing is written to the database by the scan
 * itself. The frontend's "Confirm" button is what actually persists prices
 * and purchase history, via the existing PUT /api/stores/{storeId}/prices
 * and POST /api/users/{userId}/purchases endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptScanResultDto {
    private String id;
    /** Null if no store could be resolved at all (e.g. an empty Stores table). */
    private String storeId;
    private String storeName;
    private String scannedAt; // ISO timestamp
    private List<ReceiptLineItemDto> lineItems;
}