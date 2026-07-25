package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CartRowDto;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final UserCartItemRepository userCartItemRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StorePriceRepository storePriceRepository;

    public CartService(
        UserCartItemRepository userCartItemRepository,
        ItemRepository itemRepository,
        StoreRepository storeRepository,
        UserRepository userRepository,
        StorePriceRepository storePriceRepository
    ) {
        this.userCartItemRepository = userCartItemRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storePriceRepository = storePriceRepository;
    }

    /** Returns the user's raw cart rows as DTOs; frontend does the consolidation. */
    public List<CartRowDto> getCartForUser(Long userId) {
        return userCartItemRepository.findByUserId(userId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Applies a +1/-1 delta to the "Others" bucket (sourceRecipe == null),
     * mirroring adjustOthersQuantity() in the frontend's cartLogic.ts.
     * Creates the row if it doesn't exist yet (only on increment).
     */
    public void adjustOthersQuantity(Long userId, Long itemId, Long storeId, int delta) {
        List<UserCartItem> existing = userCartItemRepository
            .findByUserIdAndItemIdAndStoreIdAndSourceRecipeIsNull(userId, itemId, storeId);

        if (existing.isEmpty()) {
            if (delta <= 0) return; // nothing to decrement

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
            Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

            UserCartItem newRow = new UserCartItem();
            newRow.setUser(user);
            newRow.setItem(item);
            newRow.setStore(store);
            newRow.setQuantity(BigDecimal.valueOf(delta));
            newRow.setSourceRecipe(null);
            userCartItemRepository.save(newRow);
            return;
        }

        UserCartItem row = existing.get(0);
        BigDecimal newQty = row.getQuantity().add(BigDecimal.valueOf(delta));
        if (newQty.compareTo(BigDecimal.ZERO) < 0) newQty = BigDecimal.ZERO;
        row.setQuantity(newQty);
        userCartItemRepository.save(row);
    }

    private CartRowDto toDto(UserCartItem row) {
        BigDecimal price = storePriceRepository
            .findByStoreId(row.getStore().getId()).stream()
            .filter(sp -> sp.getItem().getId().equals(row.getItem().getId()))
            .findFirst()
            .map(StorePrice::getPriceAmount)
            .orElse(BigDecimal.ZERO);

        return new CartRowDto(
            row.getId().toString(),
            row.getItem().getId().toString(),
            row.getItem().getName(),
            row.getStore().getId().toString(),
            row.getStore().getName(),
            price,
            row.getQuantity(),
            row.getSourceRecipe() != null ? row.getSourceRecipe().getId().toString() : null,
            row.getSourceRecipe() != null ? row.getSourceRecipe().getRecipeName() : null
        );
    }
}
