package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PUT /api/category-defaults/store. category travels in the body, not the URL path - see CategoryDefaultController's javadoc for why. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetCategoryDefaultStoreRequest {
    private String category;
    private Long storeId;
}
