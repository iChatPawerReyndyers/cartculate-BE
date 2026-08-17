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

    /** GET /api/store-prices - every known item price, across all stores. Feeds the Price Catalog view. */
    @GetMapping
    public ResponseEntity<List<StorePriceDto>> getAllPrices() {
        return ResponseEntity.ok(storePriceService.getAllPrices());
    }
}