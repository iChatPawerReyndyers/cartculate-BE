package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreateStoreRequest;
import com.ichat.cartculate.dto.StoreDto;
import com.ichat.cartculate.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /** GET /api/stores - all stores, sorted by name. Used by pickers (Add Item form, Price Catalog, Recipe modal). */
    @GetMapping
    public ResponseEntity<List<StoreDto>> getStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    /**
     * POST /api/stores - creates a new store, e.g. via ProductModal's or
     * the new-product form's "+ Add new store" option, for when the store
     * a user wants to price something at hasn't been saved yet.
     */
    @PostMapping
    public ResponseEntity<StoreDto> createStore(@RequestBody CreateStoreRequest request) {
        StoreDto created = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
