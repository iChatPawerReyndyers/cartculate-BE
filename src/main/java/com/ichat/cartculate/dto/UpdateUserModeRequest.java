package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Request body for PATCH /api/users/{userId}/mode. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserModeRequest {
    private String mode; // "HOME" | "AWAY"
}