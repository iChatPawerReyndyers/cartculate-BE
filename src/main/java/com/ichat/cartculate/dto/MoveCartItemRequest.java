package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PATCH /api/users/{userId}/cart/move - relocating an item from one store to another (e.g. "only need one thing from Puregold, might as well get it at S&R"). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveCartItemRequest {
    private Long itemId;
    private Long fromStoreId;
    private Long toStoreId;
}