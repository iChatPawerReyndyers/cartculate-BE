package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateRecipeRequest;
import com.ichat.cartculate.dto.RecipeDto;
import com.ichat.cartculate.dto.RecipeIngredientDto;
import com.ichat.cartculate.dto.UpdateRecipeRequest;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final StorePriceRepository storePriceRepository;
    private final UserStorePriceRepository userStorePriceRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final CartService cartService;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            StorePriceRepository storePriceRepository,
            UserStorePriceRepository userStorePriceRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository,
            UserCartItemRepository userCartItemRepository,
            CartService cartService
    ) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.storePriceRepository = storePriceRepository;
        this.userStorePriceRepository = userStorePriceRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.userCartItemRepository = userCartItemRepository;
        this.cartService = cartService;
    }

    public List<RecipeDto> getRecipesForUser(Long userId) {
        return recipeRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RecipeDto createRecipe(Long userId, CreateRecipeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Recipe recipe = new Recipe();
        recipe.setRecipeName(request.getName());
        recipe.setUser(user);
        // Default the multiplier to 1x (not 0x) so a freshly-created recipe
        // immediately contributes its per-batch ingredients to the Cart
        // tab, instead of silently sitting at 0x until someone manually
        // scales it up.
        recipe.setCurrentMultiplier(BigDecimal.ONE);
        recipe = recipeRepository.save(recipe);

        saveIngredients(recipe, request.getIngredients());

        return toDto(recipe);
    }

    /** PUT /api/users/{userId}/recipes/{recipeId} - edits name and replaces the ingredient list entirely. */
    public RecipeDto updateRecipe(Long recipeId, UpdateRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

        recipe.setRecipeName(request.getName());
        recipe = recipeRepository.save(recipe);

        // Replace ingredients wholesale - simpler and safer than diffing,
        // since recipe_ingredients has no other dependents besides the
        // recipe itself (user_cart_item references the recipe, not its
        // individual ingredient rows).
        recipeIngredientRepository.deleteAll(recipeIngredientRepository.findByRecipeId(recipeId));
        saveIngredients(recipe, request.getIngredients());

        return toDto(recipe);
    }

    /**
     * DELETE /api/users/{userId}/recipes/{recipeId}. Cart rows sourced from
     * this recipe aren't deleted outright - they're folded into the manual
     * "Others" bucket (source stripped, quantity kept), the same convention
     * Feature 4's unchecked-item handling uses, so removing a recipe doesn't
     * silently drop items the user still needs to buy.
     */
    public void deleteRecipe(Long recipeId) {
        List<UserCartItem> sourcedRows = userCartItemRepository.findBySourceRecipeId(recipeId);
        for (UserCartItem row : sourcedRows) {
            List<UserCartItem> othersRows = userCartItemRepository.findByUserIdAndItemIdAndStoreIdAndSourceRecipeIsNull(
                    row.getUser().getId(), row.getItem().getId(), row.getStore().getId()
            );
            if (!othersRows.isEmpty()) {
                UserCartItem others = othersRows.get(0);
                others.setQuantity(others.getQuantity().add(row.getQuantity()));
                userCartItemRepository.save(others);
                userCartItemRepository.delete(row);
            } else {
                row.setSourceRecipe(null);
                userCartItemRepository.save(row);
            }
        }

        recipeIngredientRepository.deleteAll(recipeIngredientRepository.findByRecipeId(recipeId));
        recipeRepository.deleteById(recipeId);
    }

    /**
     * PATCH .../multiplier (Feature 3): persists the new multiplier and
     * immediately syncs every ingredient's cart row to match - this is what
     * replaces the old client-side-only "Add to cart" button. Fractional
     * multipliers (x0.5, x1.5) are supported since currentMultiplier is a
     * BigDecimal, not an integer.
     */
    public RecipeDto updateMultiplier(Long userId, Long recipeId, BigDecimal multiplier) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

        recipe.setCurrentMultiplier(multiplier);
        recipe = recipeRepository.save(recipe);

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeId(recipeId);
        for (RecipeIngredient ingredient : ingredients) {
            ResolvedStore resolved = resolveStore(ingredient);
            if (resolved.store == null) continue; // no known store/price for this item yet - nothing to sync

            BigDecimal quantity = ingredient.getBaseQuantity().multiply(multiplier);
            cartService.upsertRecipeSourcedItem(userId, ingredient.getItem().getId(), resolved.store.getId(), recipeId, quantity);
        }

        return toDto(recipe);
    }

    private void saveIngredients(Recipe recipe, List<CreateRecipeRequest.IngredientInput> inputs) {
        for (CreateRecipeRequest.IngredientInput input : inputs) {
            Item item = itemRepository.findById(input.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + input.getItemId()));

            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setRecipe(recipe);
            ingredient.setItem(item);
            ingredient.setBaseQuantity(input.getBaseQuantity());
            ingredient.setUnit(input.getUnit());
            ingredient.setOptional(input.isOptional());

            if (input.getTargetStoreId() != null) {
                Store targetStore = storeRepository.findById(input.getTargetStoreId())
                        .orElseThrow(() -> new IllegalArgumentException("Store not found: " + input.getTargetStoreId()));
                ingredient.setTargetStore(targetStore);
            }

            recipeIngredientRepository.save(ingredient);
        }
    }

    private RecipeDto toDto(Recipe recipe) {
        List<RecipeIngredientDto> ingredientDtos = recipeIngredientRepository
                .findByRecipeId(recipe.getId()).stream()
                .map(this::toIngredientDto)
                .collect(Collectors.toList());

        return new RecipeDto(
                recipe.getId().toString(),
                recipe.getRecipeName(),
                ingredientDtos,
                recipe.getCurrentMultiplier()
        );
    }

    /** Small holder for a resolved store + price, shared by toIngredientDto() and updateMultiplier(). */
    private static class ResolvedStore {
        final Store store;
        final BigDecimal price;
        final boolean isCustomRouted;

        ResolvedStore(Store store, BigDecimal price, boolean isCustomRouted) {
            this.store = store;
            this.price = price;
            this.isCustomRouted = isCustomRouted;
        }
    }

    /**
     * Store routing per ingredient: prefer the ingredient's explicit
     * targetStore (custom routing). Falls back to whichever store has the
     * cheapest known price for the item if no target is set.
     *
     * Feature: personal price overrides. Uses this recipe's OWNER's
     * resolved price (their override if set, else the shared baseline)
     * for both the target-store lookup and the "find cheapest" scan -
     * otherwise a recipe's cost estimate and auto-routing could pick a
     * store based on a price the recipe's owner doesn't actually pay.
     */
    private ResolvedStore resolveStore(RecipeIngredient ingredient) {
        Item item = ingredient.getItem();
        Long userId = ingredient.getRecipe().getUser().getId();

        if (ingredient.getTargetStore() != null) {
            Store targetStore = ingredient.getTargetStore();
            BigDecimal price = resolvePriceForUser(userId, item.getId(), targetStore.getId());
            return new ResolvedStore(targetStore, price, true);
        }

        Store cheapestStore = null;
        BigDecimal cheapestPrice = null;
        for (StorePrice sp : storePriceRepository.findAll()) {
            if (!sp.getItem().getId().equals(item.getId())) continue;
            BigDecimal resolvedPrice = resolvePriceForUser(userId, item.getId(), sp.getStore().getId());
            if (cheapestPrice == null || resolvedPrice.compareTo(cheapestPrice) < 0) {
                cheapestPrice = resolvedPrice;
                cheapestStore = sp.getStore();
            }
        }
        // Also consider stores where this user ONLY has a personal
        // override (no shared baseline at all) - otherwise a
        // personal-only price could never win the "cheapest" comparison
        // just because it never showed up in the loop above.
        for (UserStorePrice override : userStorePriceRepository.findByUser_Id(userId)) {
            if (!override.getItem().getId().equals(item.getId())) continue;
            boolean alreadyConsidered = storePriceRepository.findByStoreId(override.getStore().getId()).stream()
                    .anyMatch(sp -> sp.getItem().getId().equals(item.getId()));
            if (alreadyConsidered) continue;
            if (cheapestPrice == null || override.getPriceAmount().compareTo(cheapestPrice) < 0) {
                cheapestPrice = override.getPriceAmount();
                cheapestStore = override.getStore();
            }
        }

        if (cheapestStore == null) return new ResolvedStore(null, BigDecimal.ZERO, false);
        return new ResolvedStore(cheapestStore, cheapestPrice, false);
    }

    /** Same resolution rule as CartService.resolvePriceForUser - override wins if present, else shared baseline, else zero. Duplicated rather than shared to avoid a circular dependency between the two services. */
    private BigDecimal resolvePriceForUser(Long userId, Long itemId, Long storeId) {
        return userStorePriceRepository.findByUser_IdAndItem_IdAndStore_Id(userId, itemId, storeId)
                .map(UserStorePrice::getPriceAmount)
                .orElseGet(() -> storePriceRepository.findByStoreId(storeId).stream()
                        .filter(sp -> sp.getItem().getId().equals(itemId))
                        .findFirst()
                        .map(StorePrice::getPriceAmount)
                        .orElse(BigDecimal.ZERO));
    }

    private RecipeIngredientDto toIngredientDto(RecipeIngredient ingredient) {
        Item item = ingredient.getItem();
        ResolvedStore resolved = resolveStore(ingredient);

        return new RecipeIngredientDto(
                item.getId().toString(),
                item.getName(),
                item.getCategory(),
                ingredient.getBaseQuantity(),
                ingredient.getUnit(),
                resolved.store != null ? resolved.store.getId().toString() : null,
                resolved.store != null ? resolved.store.getName() : null,
                resolved.price,
                resolved.isCustomRouted,
                ingredient.isOptional()
        );
    }
}