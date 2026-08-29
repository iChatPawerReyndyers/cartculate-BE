package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CartAdjustRequest;
import com.ichat.cartculate.dto.CartRowDto;
import com.ichat.cartculate.dto.CheckoutStatusRequest;
import com.ichat.cartculate.dto.CompleteCheckoutRequest;
import com.ichat.cartculate.dto.MoveCartItemRequest;
import com.ichat.cartculate.dto.PantryOverrideRequest;
import com.ichat.cartculate.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** PATCH /api/users/{userId}/cart/items/{cartItemId}/pantry-override - the pantry-stock '-' button. */
    @PatchMapping("/items/{cartItemId}/pantry-override")
    public ResponseEntity<CartRowDto> setPantryOverride(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestBody PantryOverrideRequest request
    ) {
        return ResponseEntity.ok(
                cartService.setPantryOverride(cartItemId, request.getOverridePantryQty(), request.getOverrideReason())
        );
    }

    /**
     * PATCH /api/users/{userId}/cart/move - long-press an item's card to
     * relocate it from one store to another (e.g. "only need one thing
     * from Puregold, might as well get it at S&R instead"). See
     * CartService.moveCartItemToStore() for what this does to
     * recipe-sourced rows and why it's a one-time override rather than a
     * permanent reroute.
     */
    @PatchMapping("/move")
    public ResponseEntity<Void> moveCartItem(
            @PathVariable Long userId,
            @RequestBody MoveCartItemRequest request
    ) {
        cartService.moveCartItemToStore(userId, request.getItemId(), request.getFromStoreId(), request.getToStoreId());
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/users/{userId}/cart/items/{cartItemId}/checkout-status - checkbox during "Start Grocery". */
    @PatchMapping("/items/{cartItemId}/checkout-status")
    public ResponseEntity<CartRowDto> setCheckoutStatus(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestBody CheckoutStatusRequest request
    ) {
        return ResponseEntity.ok(cartService.setCheckedCheckout(cartItemId, request.isChecked()));
    }

    /**
     * POST /api/users/{userId}/cart/master-reset - Feature 5's Secure Master
     * Hard-Reset Button. Per spec this wipes quantities, pantry overrides,
     * checkout checkboxes, AND all recipe multipliers back to baseline - see
     * CartService.masterReset() for the full payload. (Previously this only
     * cleared checkbox state, which didn't match the spec.)
     */
    @PostMapping("/master-reset")
    public ResponseEntity<Void> masterReset(@PathVariable Long userId) {
        cartService.masterReset(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/users/{userId}/cart/complete-checkout - called after a
     * "Done Checkout" reconciliation is confirmed. Zeroes out quantity and
     * unchecks every checked-off row for the given store, folds unchecked
     * recipe rows into "Others", and clears pantry-override data for the
     * store per Rule C. `boughtItems` (how much of each item was actually
     * bought, defaulting to the full checked quantity if omitted) lets a
     * partial purchase reduce a row instead of always zeroing it.
     */
    @PostMapping("/complete-checkout")
    public ResponseEntity<Void> completeCheckout(
            @PathVariable Long userId,
            @RequestBody CompleteCheckoutRequest request
    ) {
        Map<Long, BigDecimal> boughtQtyByItemId = new HashMap<>();
        if (request.getBoughtItems() != null) {
            for (CompleteCheckoutRequest.BoughtItemInput input : request.getBoughtItems()) {
                boughtQtyByItemId.put(input.getItemId(), input.getQuantityBought());
            }
        }
        cartService.completeCheckout(userId, request.getStoreId(), boughtQtyByItemId);
        return ResponseEntity.noContent().build();
    }
}