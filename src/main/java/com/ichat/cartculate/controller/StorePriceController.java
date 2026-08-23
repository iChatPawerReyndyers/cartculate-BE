package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.StorePriceDto;
import com.ichat.cartculate.dto.UpdateStorePricesRequest;
import com.ichat.cartculate.service.StorePriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StorePriceController {

    private final StorePriceService storePriceService;

    public StorePriceController(StorePriceService storePriceService) {
        this.storePriceService = storePriceService;
    }

    /** GET /api/stores/{storeId}/prices - all known SHARED/baseline item prices at this store (no personal overrides). */
    @GetMapping("/api/stores/{storeId}/prices")
    public ResponseEntity<List<StorePriceDto>> getPrices(@PathVariable Long storeId) {
        return ResponseEntity.ok(storePriceService.getPricesForStore(storeId));
    }

    /** PUT /api/stores/{storeId}/prices - bulk upsert SHARED/baseline prices, called by manual price entry and the receipt scanner when NOT marked personal-only. */
    @PutMapping("/api/stores/{storeId}/prices")
    public ResponseEntity<List<StorePriceDto>> updatePrices(
            @PathVariable Long storeId,
            @RequestBody UpdateStorePricesRequest request
    ) {
        return ResponseEntity.ok(storePriceService.updatePrices(storeId, request));
    }

    /** DELETE /api/stores/{storeId}/prices/{itemId} - removes one item's SHARED price at this store entirely. Does not touch any user's personal override. */
    @DeleteMapping("/api/stores/{storeId}/prices/{itemId}")
    public ResponseEntity<Void> deletePrice(
            @PathVariable Long storeId,
            @PathVariable Long itemId
    ) {
        storePriceService.deletePrice(storeId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Feature: personal price overrides - see UserStorePrice.java's
     * javadoc. itemId is in the request body/path here (not a raw
     * category-style free-text path segment), so there's no slash-in-path
     * risk like the category-defaults endpoints had.
     */
    @GetMapping("/api/users/{userId}/store-prices")
    public ResponseEntity<List<StorePriceDto>> getResolvedPricesForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(storePriceService.getResolvedPricesForUser(userId));
    }

    /** PUT /api/users/{userId}/stores/{storeId}/prices/personal - bulk upsert this user's own personal price overrides at a store. */
    @PutMapping("/api/users/{userId}/stores/{storeId}/prices/personal")
    public ResponseEntity<List<StorePriceDto>> updatePersonalPrices(
            @PathVariable Long userId,
            @PathVariable Long storeId,
            @RequestBody UpdateStorePricesRequest request
    ) {
        return ResponseEntity.ok(storePriceService.updatePersonalPrices(userId, storeId, request));
    }

    /** DELETE /api/users/{userId}/stores/{storeId}/prices/{itemId}/personal - clears this user's personal override, reverting them to the shared baseline. */
    @DeleteMapping("/api/users/{userId}/stores/{storeId}/prices/{itemId}/personal")
    public ResponseEntity<Void> clearPersonalPrice(
            @PathVariable Long userId,
            @PathVariable Long storeId,
            @PathVariable Long itemId
    ) {
        storePriceService.clearPersonalPrice(userId, storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
