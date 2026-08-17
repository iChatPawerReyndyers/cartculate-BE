package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.StorePriceDto;
import com.ichat.cartculate.dto.UpdateStorePricesRequest;
import com.ichat.cartculate.service.StorePriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/prices")
public class StorePriceController {

    private final StorePriceService storePriceService;

    public StorePriceController(StorePriceService storePriceService) {
        this.storePriceService = storePriceService;
    }

    /** GET /api/stores/{storeId}/prices - all known item prices at this store. */
    @GetMapping
    public ResponseEntity<List<StorePriceDto>> getPrices(@PathVariable Long storeId) {
        return ResponseEntity.ok(storePriceService.getPricesForStore(storeId));
    }

    /** PUT /api/stores/{storeId}/prices - bulk upsert, called by the receipt scanner's "Confirm" action. */
    @PutMapping
    public ResponseEntity<List<StorePriceDto>> updatePrices(
            @PathVariable Long storeId,
            @RequestBody UpdateStorePricesRequest request
    ) {
        return ResponseEntity.ok(storePriceService.updatePrices(storeId, request));
    }

    /** DELETE /api/stores/{storeId}/prices/{itemId} - removes one item's price at this store entirely. */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deletePrice(
            @PathVariable Long storeId,
            @PathVariable Long itemId
    ) {
        storePriceService.deletePrice(storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}