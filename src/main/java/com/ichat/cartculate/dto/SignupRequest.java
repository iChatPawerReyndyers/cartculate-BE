package com.ichat.cartculate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Matches the approved signup mockup: name, username, password only (no
 * email field shown to the user). User.email is still NOT NULL/unique at
 * the DB level though, so AuthService synthesizes a placeholder
 * ({username}@cartculate.local) under the hood - see AuthService.signup().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String name;
    private String username;
    private String password;
}
