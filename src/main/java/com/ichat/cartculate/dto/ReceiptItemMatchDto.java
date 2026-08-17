package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** One candidate match offered for a receipt line, for the frontend's "needs review" dropdown. Mirrors the frontend's ReceiptItemMatch type. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemMatchDto {
    private String itemId;
    private String itemName;
}