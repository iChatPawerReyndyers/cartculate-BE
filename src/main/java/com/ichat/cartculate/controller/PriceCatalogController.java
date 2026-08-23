package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.StorePriceDto;
import com.ichat.cartculate.service.StorePriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store-prices")
public class PriceCatalogController {

    private final StorePriceService storePriceService;

    public PriceCatalogController(StorePriceService storePriceService) {
        this.storePriceService = storePriceService;
    }

    /**
     * GET /api/store-prices - every known SHARED/baseline item price,
     * across all stores - no personal overrides applied. The Price
     * Catalog view (frontend PriceCatalogView.tsx) now calls
     * GET /api/users/{userId}/store-prices instead (StorePriceController)
     * to get personal-override-aware prices; this unscoped endpoint is
     * kept for anything that genuinely wants the raw shared baseline
     * regardless of who's asking.
     */
    @GetMapping
    public ResponseEntity<List<StorePriceDto>> getAllPrices() {
        return ResponseEntity.ok(storePriceService.getAllPrices());
    }
}