package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreateRecipeRequest;
import com.ichat.cartculate.dto.RecipeDto;
import com.ichat.cartculate.dto.UpdateRecipeMultiplierRequest;
import com.ichat.cartculate.dto.UpdateRecipeRequest;
import com.ichat.cartculate.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /** GET /api/users/{userId}/recipes - all saved recipes, base (x1) quantities. */
    @GetMapping
    public ResponseEntity<List<RecipeDto>> getRecipes(@PathVariable Long userId) {
        return ResponseEntity.ok(recipeService.getRecipesForUser(userId));
    }

    /** POST /api/users/{userId}/recipes - wires up the "+ New recipe" button. */
    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(
            @PathVariable Long userId,
            @RequestBody CreateRecipeRequest request
    ) {
        RecipeDto created = recipeService.createRecipe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/users/{userId}/recipes/{recipeId} - edits an existing recipe's name/ingredients. */
    @PutMapping("/{recipeId}")
    public ResponseEntity<RecipeDto> updateRecipe(
            @PathVariable Long userId,
            @PathVariable Long recipeId,
            @RequestBody UpdateRecipeRequest request
    ) {
        return ResponseEntity.ok(recipeService.updateRecipe(recipeId, request));
    }

    /** DELETE /api/users/{userId}/recipes/{recipeId} - deletes a recipe, folding any of its cart rows into "Others". */
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long userId,
            @PathVariable Long recipeId
    ) {
        recipeService.deleteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/users/{userId}/recipes/{recipeId}/multiplier - the recipe
     * card's +/- multiplier control. Persists the multiplier AND syncs the
     * corresponding cart rows in one call, replacing the old separate
     * "Add to cart" button.
     */
    @PatchMapping("/{recipeId}/multiplier")
    public ResponseEntity<RecipeDto> updateMultiplier(
            @PathVariable Long userId,
            @PathVariable Long recipeId,
            @RequestBody UpdateRecipeMultiplierRequest request
    ) {
        return ResponseEntity.ok(recipeService.updateMultiplier(userId, recipeId, request.getMultiplier()));
    }
}