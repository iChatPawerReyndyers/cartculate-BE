package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.ReceiptScanResultDto;
import com.ichat.cartculate.dto.ScanReceiptRequest;
import com.ichat.cartculate.service.ReceiptScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptScanController {

    private final ReceiptScanService receiptScanService;

    public ReceiptScanController(ReceiptScanService receiptScanService) {
        this.receiptScanService = receiptScanService;
    }

    /**
     * POST /api/receipts/scan - Feature 7's AI Receipt Scanner. Runs OCR + LLM
     * matching on a photographed/uploaded receipt and returns a structured,
     * per-line breakdown matched against the master Item catalog.
     *
     * This is read-only / preview-only: nothing is persisted here. The
     * frontend's existing "Confirm" action (PUT /api/stores/{storeId}/prices
     * + POST /api/users/{userId}/purchases) is what actually saves the
     * result, exactly as it already does for the old mocked flow.
     */
    @PostMapping("/scan")
    public ResponseEntity<ReceiptScanResultDto> scanReceipt(@RequestBody ScanReceiptRequest request) {
        return ResponseEntity.ok(receiptScanService.scanReceipt(request));
    }
}