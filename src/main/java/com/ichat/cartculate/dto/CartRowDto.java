package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Mirrors the frontend's CartRow interface (src/types/index.ts) field-for-field,
 * so GET /api/users/{userId}/cart can be dropped straight into cartLogic.consolidateCart().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartRowDto {
    private String id;
    private String itemId;
    private String itemName;
    private String storeId;
    private String storeName;
    private BigDecimal price;
    private BigDecimal quantity;
    private String sourceRecipeId;
    private String sourceRecipeName;
}
