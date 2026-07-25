package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreatePurchasesRequest;
import com.ichat.cartculate.dto.PurchaseRecordDto;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public PurchaseHistoryService(
        PurchaseHistoryRepository purchaseHistoryRepository,
        UserRepository userRepository,
        ItemRepository itemRepository,
        StoreRepository storeRepository
    ) {
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    /** Feeds every Insights chart: budget, store comparison, category breakdown, price trend. */
    public List<PurchaseRecordDto> getPurchasesForUser(Long userId) {
        return purchaseHistoryRepository.findByUserId(userId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /** Bulk insert - used by the receipt scanner's "Confirm" action and manual "mark as bought". */
    public List<PurchaseRecordDto> createPurchases(Long userId, CreatePurchasesRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return request.getPurchases().stream()
            .map(input -> {
                Item item = itemRepository.findById(input.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + input.getItemId()));
                Store store = storeRepository.findById(input.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("Store not found: " + input.getStoreId()));

                PurchaseHistory record = new PurchaseHistory();
                record.setUser(user);
                record.setItem(item);
                record.setStore(store);
                record.setQuantityBought(input.getQuantityBought());
                record.setPricePerUnit(input.getPricePerUnit());
                record.setPurchaseDate(
                    input.getPurchaseDate() != null
                        ? LocalDateTime.parse(input.getPurchaseDate(), DateTimeFormatter.ISO_DATE_TIME)
                        : LocalDateTime.now()
                );

                return toDto(purchaseHistoryRepository.save(record));
            })
            .collect(Collectors.toList());
    }

    private PurchaseRecordDto toDto(PurchaseHistory record) {
        return new PurchaseRecordDto(
            record.getId().toString(),
            record.getUser().getId().toString(),
            record.getItem().getId().toString(),
            record.getItem().getName(),
            record.getItem().getCategory(),
            record.getStore().getId().toString(),
            record.getStore().getName(),
            record.getQuantityBought(),
            record.getPricePerUnit(),
            record.getPurchaseDate().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
