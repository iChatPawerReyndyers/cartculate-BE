package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateRecipeRequest;
import com.ichat.cartculate.dto.RecipeDto;
import com.ichat.cartculate.dto.RecipeIngredientDto;
import com.ichat.cartculate.dto.UpdateRecipeRequest;
import com.ichat.cartculate.entity.*;
import com.ichat.cartculate.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final StorePriceRepository storePriceRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final CartService cartService;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            StorePriceRepository storePriceRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            StoreRepository storeRepository,
            UserCartItemRepository userCartItemRepository,
            CartService cartService
    ) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.storePriceRepository = storePriceRepository;
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
        recipe.setCurrentMultiplier(BigDecimal.ZERO);
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
     */
    private ResolvedStore resolveStore(RecipeIngredient ingredient) {
        Item item = ingredient.getItem();

        if (ingredient.getTargetStore() != null) {
            Store targetStore = ingredient.getTargetStore();
            BigDecimal price = storePriceRepository
                    .findByStoreId(targetStore.getId()).stream()
                    .filter(sp -> sp.getItem().getId().equals(item.getId()))
                    .findFirst()
                    .map(StorePrice::getPriceAmount)
                    .orElse(BigDecimal.ZERO);
            return new ResolvedStore(targetStore, price, true);
        }

        StorePrice cheapest = storePriceRepository.findAll().stream()
                .filter(sp -> sp.getItem().getId().equals(item.getId()))
                .min(Comparator.comparing(StorePrice::getPriceAmount))
                .orElse(null);

        if (cheapest == null) return new ResolvedStore(null, BigDecimal.ZERO, false);
        return new ResolvedStore(cheapest.getStore(), cheapest.getPriceAmount(), false);
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