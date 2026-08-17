package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for POST /api/receipts/scan - one receipt photo, base64-encoded. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanReceiptRequest {
    /** Raw base64 image bytes - just the encoded bytes, no "data:image/..." prefix. */
    private String imageBase64;
    /** e.g. "image/jpeg", "image/png" - defaults to image/jpeg on the backend if omitted. */
    private String mediaType;
}