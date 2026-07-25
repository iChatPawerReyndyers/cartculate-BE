package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PATCH /api/users/{userId}/cart/adjust - a single +1/-1 tap. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartAdjustRequest {
    private Long itemId;
    private Long storeId;
    private int delta; // +1 or -1
}
