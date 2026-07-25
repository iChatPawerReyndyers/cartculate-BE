package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CartAdjustRequest;
import com.ichat.cartculate.dto.CartRowDto;
import com.ichat.cartculate.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** GET /api/users/{userId}/cart - raw cart rows, ready for cartLogic.consolidateCart() on the frontend. */
    @GetMapping
    public ResponseEntity<List<CartRowDto>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartForUser(userId));
    }

    /** PATCH /api/users/{userId}/cart/adjust - a single +1/-1 tap on the main screen. */
    @PatchMapping("/adjust")
    public ResponseEntity<Void> adjustCart(
        @PathVariable Long userId,
        @RequestBody CartAdjustRequest request
    ) {
        cartService.adjustOthersQuantity(userId, request.getItemId(), request.getStoreId(), request.getDelta());
        return ResponseEntity.noContent().build();
    }
}
