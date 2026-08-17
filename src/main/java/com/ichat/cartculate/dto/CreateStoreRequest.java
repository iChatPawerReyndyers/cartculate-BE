package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for POST /api/stores - adding a new store, e.g. via ProductModal's "+ Add new store" option. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {
    private String name;
}
