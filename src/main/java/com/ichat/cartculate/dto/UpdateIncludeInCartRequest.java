package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PATCH /api/items/{itemId}/include-in-cart. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncludeInCartRequest {
    private boolean includeInCart;
}
