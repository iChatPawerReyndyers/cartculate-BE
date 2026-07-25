package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreateRecipeRequest;
import com.ichat.cartculate.dto.RecipeDto;
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
}
