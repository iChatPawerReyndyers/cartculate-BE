package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * One parsed line from a scanned receipt, matched against the master Item
 * catalog by the AI. Mirrors the frontend's ReceiptLineItem type exactly -
 * see ReceiptLineItemCard.tsx for how needsReview/alternativeMatches render.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptLineItemDto {
    private String id;
    /** The raw OCR'd text as printed on the receipt, e.g. "CK LT 1.5". */
    private String rawText;
    /** Null when the AI couldn't confidently match anything in the catalog. */
    private String matchedItemId;
    private String matchedItemName;
    private String category;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    /** True = low-confidence match, or no match at all - the frontend shows the review dropdown. */
    private boolean needsReview;
    private List<ReceiptItemMatchDto> alternativeMatches;
}