package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateRecipeRequest;
import com.ichat.cartculate.dto.RecipeDto;
import com.ichat.cartculate.dto.RecipeIngredientDto;
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

    public RecipeService(
        RecipeRepository recipeRepository,
        RecipeIngredientRepository recipeIngredientRepository,
        StorePriceRepository storePriceRepository,
        UserRepository userRepository,
        ItemRepository itemRepository
    ) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.storePriceRepository = storePriceRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
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
        recipe = recipeRepository.save(recipe);

        for (CreateRecipeRequest.IngredientInput input : request.getIngredients()) {
            Item item = itemRepository.findById(input.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + input.getItemId()));

            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setRecipe(recipe);
            ingredient.setItem(item);
            ingredient.setBaseQuantity(input.getBaseQuantity());
            ingredient.setUnit(input.getUnit());
            recipeIngredientRepository.save(ingredient);
        }

        return toDto(recipe);
    }

    private RecipeDto toDto(Recipe recipe) {
        List<RecipeIngredientDto> ingredientDtos = recipeIngredientRepository
            .findByRecipeId(recipe.getId()).stream()
            .map(this::toIngredientDto)
            .collect(Collectors.toList());

        return new RecipeDto(recipe.getId().toString(), recipe.getRecipeName(), ingredientDtos);
    }

    /**
     * TODO: "default store" is currently just whichever store has the lowest
     * known price for this item. Once users can set a preferred/home store,
     * this should prefer that store instead of always picking cheapest.
     */
    private RecipeIngredientDto toIngredientDto(RecipeIngredient ingredient) {
        Item item = ingredient.getItem();

        StorePrice cheapest = storePriceRepository.findAll().stream()
            .filter(sp -> sp.getItem().getId().equals(item.getId()))
            .min(Comparator.comparing(StorePrice::getPriceAmount))
            .orElse(null);

        String defaultStoreId = cheapest != null ? cheapest.getStore().getId().toString() : null;
        String defaultStoreName = cheapest != null ? cheapest.getStore().getName() : null;
        BigDecimal defaultPrice = cheapest != null ? cheapest.getPriceAmount() : BigDecimal.ZERO;

        return new RecipeIngredientDto(
            item.getId().toString(),
            item.getName(),
            ingredient.getBaseQuantity(),
            ingredient.getUnit(),
            defaultStoreId,
            defaultStoreName,
            defaultPrice
        );
    }
}
