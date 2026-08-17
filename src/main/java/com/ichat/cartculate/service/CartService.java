package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CartRowDto;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final UserCartItemRepository userCartItemRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StorePriceRepository storePriceRepository;
    private final RecipeRepository recipeRepository;

    public CartService(
            UserCartItemRepository userCartItemRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StorePriceRepository storePriceRepository,
            RecipeRepository recipeRepository
    ) {
        this.userCartItemRepository = userCartItemRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storePriceRepository = storePriceRepository;
        this.recipeRepository = recipeRepository;
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
            newRow.setOverridePantryQty(BigDecimal.ZERO);
            newRow.setIsCheckedCheckout(false);
            userCartItemRepository.save(newRow);
            return;
        }

        UserCartItem row = existing.get(0);
        BigDecimal newQty = row.getQuantity().add(BigDecimal.valueOf(delta));
        if (newQty.compareTo(BigDecimal.ZERO) < 0) newQty = BigDecimal.ZERO;
        row.setQuantity(newQty);
        userCartItemRepository.save(row);
    }

    /**
     * Recipe-multiplier cart sync (Feature 3): called by RecipeService whenever
     * a recipe's multiplier changes, once per ingredient. Upserts the cart row
     * sourced from this recipe for this item+store to the given quantity -
     * replacing the old client-side-only "Add to cart" merge with a real,
     * persisted write. quantity of 0 updates an existing row to 0 (same
     * "excluded" convention used elsewhere) rather than deleting it, but does
     * NOT create a new zero-quantity row if none exists yet.
     *
     * Per spec Rule B (Recipe Scaling Snapshot Isolation): this only ever
     * touches `quantity`. overridePantryQty/overrideReason are a separate,
     * static, manually-set value and are never recalculated here - so
     * bumping a recipe from x1 to x3 recomputes the ingredient quantity
     * against the NEW multiplier while the user's pantry deduction stays
     * exactly as they set it.
     */
    public void upsertRecipeSourcedItem(Long userId, Long itemId, Long storeId, Long recipeId, BigDecimal quantity) {
        List<UserCartItem> existing = userCartItemRepository
                .findByUserIdAndItemIdAndStoreIdAndSourceRecipeId(userId, itemId, storeId, recipeId);

        if (!existing.isEmpty()) {
            UserCartItem row = existing.get(0);
            row.setQuantity(quantity);
            userCartItemRepository.save(row);
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) return; // nothing to create

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

        UserCartItem newRow = new UserCartItem();
        newRow.setUser(user);
        newRow.setItem(item);
        newRow.setStore(store);
        newRow.setSourceRecipe(recipe);
        newRow.setQuantity(quantity);
        newRow.setOverridePantryQty(BigDecimal.ZERO);
        newRow.setIsCheckedCheckout(false);
        userCartItemRepository.save(newRow);
    }

    /**
     * Sets the pantry override for a specific cart row (Feature 2's
     * "Inventory Mitigation Engine" - the "Pantry Treasure Found" popup).
     * Reducing overridePantryQty below zero is rejected; capping above the
     * row's quantity is intentionally NOT enforced here, matching the
     * spec's "Full Stock Reduction (Quantity = 0)" note - the frontend is
     * expected to reconcile the resulting "need to buy" math via cartLogic.
     */
    public CartRowDto setPantryOverride(Long cartItemId, BigDecimal overridePantryQty, String overrideReason) {
        UserCartItem row = userCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (overridePantryQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("overridePantryQty cannot be negative");
        }

        row.setOverridePantryQty(overridePantryQty);
        row.setOverrideReason(overrideReason);
        return toDto(userCartItemRepository.save(row));
    }

    /** Toggles the checkbox state during "Start Grocery" (Away Mode). */
    public CartRowDto setCheckedCheckout(Long cartItemId, boolean checked) {
        UserCartItem row = userCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));
        row.setIsCheckedCheckout(checked);
        return toDto(userCartItemRepository.save(row));
    }

    /**
     * Feature 5 - Secure Master Hard-Reset Button. Per spec's "Reset Action
     * Payload": zeroes every cart row's quantity AND its pantry-override
     * snapshot (qty + reason), clears every checkout checkbox, and resets
     * every one of the user's recipe multipliers back to 0 - a full wipe
     * back to the baseline grid, not just a checkbox clear.
     *
     * NOTE: this used to be named resetCheckoutState() and only cleared
     * isCheckedCheckout flags, which didn't match the spec at all (it left
     * quantities, pantry data, and recipe multipliers untouched). Renamed
     * to masterReset() to reflect what it actually does now; CartController
     * calls this from POST /api/users/{userId}/cart/master-reset.
     */
    public void masterReset(Long userId) {
        List<UserCartItem> rows = userCartItemRepository.findByUserId(userId);
        for (UserCartItem row : rows) {
            row.setQuantity(BigDecimal.ZERO);
            row.setOverridePantryQty(BigDecimal.ZERO);
            row.setOverrideReason(null);
            row.setIsCheckedCheckout(false);
        }
        userCartItemRepository.saveAll(rows);

        List<Recipe> recipes = recipeRepository.findByUserId(userId);
        for (Recipe recipe : recipes) {
            recipe.setCurrentMultiplier(BigDecimal.ZERO);
        }
        recipeRepository.saveAll(recipes);
    }

    /**
     * Trip Conclusion (Feature 6), now also covering two previously-missing
     * Feature 4 rules, plus Rule C (One-Time Session Cache Reset):
     *
     * 1. Checked rows at this store: reduced by however much of
     *    `boughtQtyByItemId` was actually bought (defaults to the row's
     *    full checked quantity if the item isn't present in the map, e.g.
     *    older clients that don't send it) - NOT unconditionally zeroed.
     *    A single item can span multiple rows if it's sourced from more
     *    than one recipe, so the bought quantity is distributed across
     *    that item's checked rows in order until it's exhausted, leaving
     *    any remainder as real, still-needed quantity. Every checked row
     *    gets unchecked regardless of how much was reduced, since the trip
     *    attempt for it is over either way.
     * 2. Unchecked-item handling: any recipe-sourced row at this store that
     *    was NOT checked off gets its source stripped and folded into the
     *    manual "Others" row for the same item+store (merging quantities if
     *    an Others row already exists), so it carries over to the next trip
     *    without still being tied to the recipe.
     * 3. Recipe multiplier cascade: for every recipe touched by #1 or #2, if
     *    ALL of that recipe's remaining cart rows are now quantity 0 (fully
     *    checked out or folded away), the recipe's currentMultiplier resets
     *    to 0 - its card then shows "not currently in cart". A partially
     *    bought item correctly does NOT cascade, since real quantity remains.
     * 4. Rule C session cache reset: pantry stock overrides are strictly
     *    single-trip session variables. Once this store's trip is committed
     *    to purchase_history, override_pantry_qty resets to 0.000 and
     *    override_reason resets to NULL across every row at this store.
     */
    public void completeCheckout(Long userId, Long storeId, Map<Long, BigDecimal> boughtQtyByItemId) {
        // The Away Mode UI shows a single checkbox per ITEM (not one per
        // recipe source), so isCheckedCheckout=true only ever lives on one
        // representative row per item. This finds WHICH items were checked...
        List<UserCartItem> checkedRepresentativeRows = userCartItemRepository
                .findByUserIdAndStoreIdAndIsCheckedCheckoutTrue(userId, storeId);
        Set<Long> checkedItemIds = checkedRepresentativeRows.stream()
                .map(r -> r.getItem().getId())
                .collect(Collectors.toSet());

        Set<Long> affectedRecipeIds = new HashSet<>();

        // ...then gathers ALL of those items' rows at this store (every
        // recipe source + manual "Others"), since checking an item means
        // "I'm done with this item as a whole" - the bought-quantity
        // distribution needs to span every contributing row, not just
        // whichever one happened to carry the checkbox flag.
        List<UserCartItem> rowsForCheckedItems = userCartItemRepository.findByUserId(userId).stream()
                .filter(r -> r.getStore().getId().equals(storeId))
                .filter(r -> checkedItemIds.contains(r.getItem().getId()))
                .collect(Collectors.toList());

        Map<Long, List<UserCartItem>> rowsByItemId = rowsForCheckedItems.stream()
                .collect(Collectors.groupingBy(r -> r.getItem().getId()));

        for (Map.Entry<Long, List<UserCartItem>> entry : rowsByItemId.entrySet()) {
            Long itemId = entry.getKey();
            List<UserCartItem> rows = entry.getValue();

            BigDecimal totalQty = rows.stream()
                    .map(UserCartItem::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal remainingBought = boughtQtyByItemId.getOrDefault(itemId, totalQty);
            if (remainingBought.compareTo(BigDecimal.ZERO) < 0) remainingBought = BigDecimal.ZERO;

            for (UserCartItem row : rows) {
                if (row.getSourceRecipe() != null) affectedRecipeIds.add(row.getSourceRecipe().getId());

                BigDecimal reduceBy = remainingBought.min(row.getQuantity());
                row.setQuantity(row.getQuantity().subtract(reduceBy));
                remainingBought = remainingBought.subtract(reduceBy);
                row.setIsCheckedCheckout(false);
            }
        }
        userCartItemRepository.saveAll(rowsForCheckedItems);

        // Unchecked-item handling: recipe-sourced rows at this store for
        // items that were NEVER checked at all (explicitly excludes items
        // handled above - a partially-bought checked item's leftover
        // remainder is still genuinely needed by its recipe, so it must NOT
        // be treated the same as an item the user skipped entirely).
        List<UserCartItem> uncheckedRecipeRows = userCartItemRepository.findByUserId(userId).stream()
                .filter(r -> r.getStore().getId().equals(storeId))
                .filter(r -> r.getSourceRecipe() != null)
                .filter(r -> !checkedItemIds.contains(r.getItem().getId()))
                .filter(r -> r.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        for (UserCartItem row : uncheckedRecipeRows) {
            affectedRecipeIds.add(row.getSourceRecipe().getId());

            List<UserCartItem> othersRows = userCartItemRepository
                    .findByUserIdAndItemIdAndStoreIdAndSourceRecipeIsNull(userId, row.getItem().getId(), storeId);

            if (!othersRows.isEmpty()) {
                UserCartItem others = othersRows.get(0);
                others.setQuantity(others.getQuantity().add(row.getQuantity()));
                userCartItemRepository.save(others);
                userCartItemRepository.delete(row); // merged into the Others row above
            } else {
                row.setSourceRecipe(null); // this row itself becomes the Others row
                userCartItemRepository.save(row);
            }
        }

        // Recipe multiplier cascade.
        for (Long recipeId : affectedRecipeIds) {
            List<UserCartItem> remaining = userCartItemRepository.findBySourceRecipeId(recipeId);
            boolean allResolved = remaining.stream()
                    .allMatch(r -> r.getQuantity().compareTo(BigDecimal.ZERO) == 0);
            if (allResolved) {
                recipeRepository.findById(recipeId).ifPresent(recipe -> {
                    recipe.setCurrentMultiplier(BigDecimal.ZERO);
                    recipeRepository.save(recipe);
                });
            }
        }

        // Rule C - One-Time Session Cache Reset. Re-fetch this store's rows
        // fresh (some of the objects above were deleted/merged) and clear
        // every remaining row's pantry-override snapshot for the NEXT cycle.
        List<UserCartItem> storeRowsAfterCleanup = userCartItemRepository.findByUserId(userId).stream()
                .filter(r -> r.getStore().getId().equals(storeId))
                .collect(Collectors.toList());
        for (UserCartItem row : storeRowsAfterCleanup) {
            row.setOverridePantryQty(BigDecimal.ZERO);
            row.setOverrideReason(null);
        }
        userCartItemRepository.saveAll(storeRowsAfterCleanup);
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
                row.getItem().getCategory(),
                row.getItem().getUnit(),
                row.getStore().getId().toString(),
                row.getStore().getName(),
                price,
                row.getQuantity(),
                row.getSourceRecipe() != null ? row.getSourceRecipe().getId().toString() : null,
                row.getSourceRecipe() != null ? row.getSourceRecipe().getRecipeName() : null,
                row.getOverridePantryQty(),
                row.getOverrideReason(),
                row.getIsCheckedCheckout() != null && row.getIsCheckedCheckout()
        );
    }
}