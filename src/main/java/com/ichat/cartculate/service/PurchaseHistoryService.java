package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreatePurchaseRequest;
import com.ichat.cartculate.dto.PurchaseRecordDto;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final JsonMapper jsonMapper;

    public PurchaseHistoryService(
            PurchaseHistoryRepository purchaseHistoryRepository,
            UserRepository userRepository,
            StoreRepository storeRepository,
            JsonMapper jsonMapper
    ) {
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.jsonMapper = jsonMapper;
    }

    /** GET /api/users/{userId}/purchases - receipt-level history, per the updated spec. */
    public List<PurchaseRecordDto> getPurchasesForUser(Long userId) {
        return purchaseHistoryRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Archives one receipt at Checkout completion. The structured `items`
     * list is serialized into itemManifestJson - see CreatePurchaseRequest
     * for why this is done server-side rather than requiring callers to
     * build the JSON string themselves.
     */
    public PurchaseRecordDto createPurchase(Long userId, CreatePurchaseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + request.getStoreId()));

        String manifestJson;
        try {
            manifestJson = jsonMapper.writeValueAsString(request.getItems());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize item manifest", e);
        }

        PurchaseHistory record = new PurchaseHistory();
        record.setUser(user);
        record.setStore(store);
        record.setTotalReceiptSpent(request.getTotalReceiptSpent());
        record.setPurchaseDate(
                request.getPurchaseDate() != null
                        ? LocalDateTime.parse(request.getPurchaseDate(), DateTimeFormatter.ISO_DATE_TIME)
                        : LocalDateTime.now()
        );
        record.setItemManifestJson(manifestJson);

        return toDto(purchaseHistoryRepository.save(record));
    }

    private PurchaseRecordDto toDto(PurchaseHistory record) {
        return new PurchaseRecordDto(
                record.getId().toString(),
                record.getUser() != null ? record.getUser().getId().toString() : null,
                record.getStore().getId().toString(),
                record.getStore().getName(),
                record.getTotalReceiptSpent(),
                record.getPurchaseDate().format(DateTimeFormatter.ISO_DATE_TIME),
                record.getItemManifestJson()
        );
    }
}