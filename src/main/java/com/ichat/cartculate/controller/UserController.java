package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.UpdateUserModeRequest;
import com.ichat.cartculate.dto.UserDto;
import com.ichat.cartculate.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users/{userId} - includes the persisted Home/Away mode. */
    @GetMapping
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /** PATCH /api/users/{userId}/mode - toggles Home/Away mode, persisted server-side. */
    @PatchMapping("/mode")
    public ResponseEntity<UserDto> updateMode(
            @PathVariable Long userId,
            @RequestBody UpdateUserModeRequest request
    ) {
        return ResponseEntity.ok(userService.updateMode(userId, request.getMode()));
    }
}