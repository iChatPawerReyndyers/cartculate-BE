package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.StorePriceDto;
import com.ichat.cartculate.dto.UpdateStorePricesRequest;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorePriceService {

    private final StorePriceRepository storePriceRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public StorePriceService(
        StorePriceRepository storePriceRepository,
        ItemRepository itemRepository,
        StoreRepository storeRepository
    ) {
        this.storePriceRepository = storePriceRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    public List<StorePriceDto> getPricesForStore(Long storeId) {
        return storePriceRepository.findByStoreId(storeId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Upserts prices for a store - one row per item. Used by the receipt
     * scanner's "Confirm" action (buildStorePriceUpdates() on the frontend)
     * and by manual per-store price entry (Feature 1's Per-Store Pricing Matrix).
     */
    public List<StorePriceDto> updatePrices(Long storeId, UpdateStorePricesRequest request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        return request.getUpdates().stream()
            .map(update -> {
                Item item = itemRepository.findById(update.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + update.getItemId()));

                StorePriceId id = new StorePriceId(item.getId(), store.getId());
                StorePrice storePrice = storePriceRepository.findById(id)
                    .orElseGet(() -> new StorePrice(id, item, store, update.getPriceAmount()));
                storePrice.setPriceAmount(update.getPriceAmount());

                return toDto(storePriceRepository.save(storePrice));
            })
            .collect(Collectors.toList());
    }

    private StorePriceDto toDto(StorePrice storePrice) {
        return new StorePriceDto(
            storePrice.getItem().getId().toString(),
            storePrice.getItem().getName(),
            storePrice.getStore().getId().toString(),
            storePrice.getPriceAmount()
        );
    }
}
