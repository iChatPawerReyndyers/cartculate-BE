package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Response shape for GET /api/category-defaults. storeId/storeName are null if no default store is set for this category. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDefaultDto {
    private String category;
    private String storeId;
    private String storeName;
    private boolean defaultIsIngredient;
}
