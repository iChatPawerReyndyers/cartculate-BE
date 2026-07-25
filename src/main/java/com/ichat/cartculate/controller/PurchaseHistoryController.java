package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreatePurchasesRequest;
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

    /** GET /api/users/{userId}/purchases - feeds the Insights tab's charts. */
    @GetMapping
    public ResponseEntity<List<PurchaseRecordDto>> getPurchases(@PathVariable Long userId) {
        return ResponseEntity.ok(purchaseHistoryService.getPurchasesForUser(userId));
    }

    /** POST /api/users/{userId}/purchases - bulk insert (receipt confirm, "mark as bought"). */
    @PostMapping
    public ResponseEntity<List<PurchaseRecordDto>> createPurchases(
        @PathVariable Long userId,
        @RequestBody CreatePurchasesRequest request
    ) {
        List<PurchaseRecordDto> created = purchaseHistoryService.createPurchases(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
