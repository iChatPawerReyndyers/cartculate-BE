package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.LoginRequest;
import com.ichat.cartculate.dto.SignupRequest;
import com.ichat.cartculate.dto.UserDto;
import com.ichat.cartculate.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login - username + password only, per the login screen. 401 on bad credentials. */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** POST /api/auth/signup - name + username + password, from the "Create an account" link. 409 if username taken. */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }
}
