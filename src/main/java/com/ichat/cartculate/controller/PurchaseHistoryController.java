package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreatePurchaseRequest;
import com.ichat.cartculate.dto.PurchaseRecordDto;
import com.ichat.cartculate.service.PurchaseHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/purchases")
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;

    public PurchaseHistoryController(PurchaseHistoryService purchaseHistoryService) {
        this.purchaseHistoryService = purchaseHistoryService;
    }

    /** GET /api/users/{userId}/purchases - receipt-level history. */
    @GetMapping
    public ResponseEntity<List<PurchaseRecordDto>> getPurchases(@PathVariable Long userId) {
        return ResponseEntity.ok(purchaseHistoryService.getPurchasesForUser(userId));
    }

    /** POST /api/users/{userId}/purchases - archives ONE receipt at Checkout completion. */
    @PostMapping
    public ResponseEntity<PurchaseRecordDto> createPurchase(
            @PathVariable Long userId,
            @RequestBody CreatePurchaseRequest request
    ) {
        PurchaseRecordDto created = purchaseHistoryService.createPurchase(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}