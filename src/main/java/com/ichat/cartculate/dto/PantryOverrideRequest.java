package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/** Request body for PATCH /api/users/{userId}/cart/items/{cartItemId}/pantry-override. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PantryOverrideRequest {
    private BigDecimal overridePantryQty;
    private String overrideReason; // e.g. "Freezer Find", "Pantry Stock"
}