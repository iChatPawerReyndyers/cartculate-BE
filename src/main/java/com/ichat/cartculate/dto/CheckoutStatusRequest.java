package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PATCH /api/users/{userId}/cart/items/{cartItemId}/checkout-status. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutStatusRequest {
    private boolean checked;
}