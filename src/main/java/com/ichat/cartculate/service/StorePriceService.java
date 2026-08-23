package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.StorePriceDto;
import com.ichat.cartculate.dto.UpdateStorePricesRequest;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StorePriceService {

    private final StorePriceRepository storePriceRepository;
    private final UserStorePriceRepository userStorePriceRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StorePriceService(
            StorePriceRepository storePriceRepository,
            UserStorePriceRepository userStorePriceRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository,
            UserRepository userRepository
    ) {
        this.storePriceRepository = storePriceRepository;
        this.userStorePriceRepository = userStorePriceRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public List<StorePriceDto> getPricesForStore(Long storeId) {
        return storePriceRepository.findByStoreId(storeId).stream()
                .map(sp -> toDto(sp.getItem(), sp.getStore(), sp.getPriceAmount(), sp.getPriceSource(), false))
                .collect(Collectors.toList());
    }

    /** All known SHARED/baseline prices across every store and item, ignoring any user's personal overrides. Kept for the unscoped /api/store-prices endpoint. */
    public List<StorePriceDto> getAllPrices() {
        return storePriceRepository.findAll().stream()
                .map(sp -> toDto(sp.getItem(), sp.getStore(), sp.getPriceAmount(), sp.getPriceSource(), false))
                .collect(Collectors.toList());
    }

    /**
     * GET /api/users/{userId}/store-prices - feature: personal price
     * overrides. Every shared/baseline StorePrice row is included as-is,
     * UNLESS this user has their own override for that exact item+store,
     * in which case the override's price/source wins and isPersonalOverride=true
     * on the returned row. A user can also have an override for an
     * item+store that has no shared baseline at all (a fully personal
     * price nobody else has ever set) - those are included too.
     */
    public List<StorePriceDto> getResolvedPricesForUser(Long userId) {
        List<StorePrice> baseline = storePriceRepository.findAll();
        List<UserStorePrice> overrides = userStorePriceRepository.findByUser_Id(userId);

        Map<String, UserStorePrice> overrideByKey = new HashMap<>();
        for (UserStorePrice override : overrides) {
            overrideByKey.put(key(override.getItem().getId(), override.getStore().getId()), override);
        }

        List<StorePriceDto> result = new ArrayList<>();
        Set<String> covered = new HashSet<>();

        for (StorePrice sp : baseline) {
            String k = key(sp.getItem().getId(), sp.getStore().getId());
            covered.add(k);
            UserStorePrice override = overrideByKey.get(k);
            if (override != null) {
                result.add(toDto(override.getItem(), override.getStore(), override.getPriceAmount(), override.getPriceSource(), true));
            } else {
                result.add(toDto(sp.getItem(), sp.getStore(), sp.getPriceAmount(), sp.getPriceSource(), false));
            }
        }

        for (UserStorePrice override : overrides) {
            String k = key(override.getItem().getId(), override.getStore().getId());
            if (!covered.contains(k)) {
                result.add(toDto(override.getItem(), override.getStore(), override.getPriceAmount(), override.getPriceSource(), true));
            }
        }

        return result;
    }

    /**
     * Upserts SHARED/baseline prices for a store - one row per item. Used
     * by the receipt scanner's "Confirm" action when the person hasn't
     * marked the price as personal-only, and by manual per-store price
     * entry (Feature 1's Per-Store Pricing Matrix).
     */
    public List<StorePriceDto> updatePrices(Long storeId, UpdateStorePricesRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        return request.getUpdates().stream()
                .map(update -> {
                    Item item = itemRepository.findById(update.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + update.getItemId()));

                    PriceSource source = update.getSource() != null ? update.getSource() : PriceSource.MANUAL;

                    StorePriceId id = new StorePriceId(item.getId(), store.getId());
                    StorePrice storePrice = storePriceRepository.findById(id)
                            .orElseGet(() -> new StorePrice(id, item, store, update.getPriceAmount(), source));
                    storePrice.setPriceAmount(update.getPriceAmount());
                    storePrice.setPriceSource(source);
                    StorePrice saved = storePriceRepository.save(storePrice);

                    return toDto(saved.getItem(), saved.getStore(), saved.getPriceAmount(), saved.getPriceSource(), false);
                })
                .collect(Collectors.toList());
    }

    /**
     * PUT /api/users/{userId}/stores/{storeId}/prices/personal - bulk
     * upsert this user's own PERSONAL overrides at a store, mirroring
     * updatePrices() above but writing to UserStorePrice instead of the
     * shared StorePrice. Used by the receipt scanner's "Confirm" action
     * (a scanned receipt is inherently this user's own purchase - it
     * shouldn't silently change what everyone else sees as "the" price),
     * and by the "this is different for me" toggle on a manual price row.
     */
    public List<StorePriceDto> updatePersonalPrices(Long userId, Long storeId, UpdateStorePricesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        return request.getUpdates().stream()
                .map(update -> {
                    Item item = itemRepository.findById(update.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + update.getItemId()));

                    PriceSource source = update.getSource() != null ? update.getSource() : PriceSource.MANUAL;

                    UserStorePriceId id = new UserStorePriceId(userId, item.getId(), store.getId());
                    UserStorePrice override = userStorePriceRepository.findById(id)
                            .orElseGet(() -> new UserStorePrice(id, user, item, store, update.getPriceAmount(), source));
                    override.setPriceAmount(update.getPriceAmount());
                    override.setPriceSource(source);
                    UserStorePrice saved = userStorePriceRepository.save(override);

                    return toDto(saved.getItem(), saved.getStore(), saved.getPriceAmount(), saved.getPriceSource(), true);
                })
                .collect(Collectors.toList());
    }

    /** DELETE /api/users/{userId}/stores/{storeId}/prices/{itemId}/personal - clears this user's personal override, reverting them to the shared baseline (if any). */
    public void clearPersonalPrice(Long userId, Long storeId, Long itemId) {
        userStorePriceRepository.deleteById(new UserStorePriceId(userId, itemId, storeId));
    }

    /** Removes a single item's SHARED price at a store entirely - used by the Price Catalog editor's "remove store price" action. Does not touch any user's personal override for that item/store. */
    public void deletePrice(Long storeId, Long itemId) {
        StorePriceId id = new StorePriceId(itemId, storeId);
        storePriceRepository.deleteById(id);
    }

    private String key(Long itemId, Long storeId) {
        return itemId + ":" + storeId;
    }

    private StorePriceDto toDto(Item item, Store store, BigDecimal priceAmount, PriceSource priceSource, boolean isPersonalOverride) {
        return new StorePriceDto(
                item.getId().toString(),
                item.getName(),
                store.getId().toString(),
                store.getName(),
                priceAmount,
                priceSource != null ? priceSource.name() : PriceSource.MANUAL.name(),
                isPersonalOverride
        );
    }
}