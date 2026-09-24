package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CategoryDefaultDto;
import com.ichat.cartculate.dto.SetCategoryDefaultIsIngredientRequest;
import com.ichat.cartculate.dto.SetCategoryDefaultStoreRequest;
import com.ichat.cartculate.service.CategoryDefaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Per-user (see UserCategoryDefault's javadoc for why this moved off the
 * old app-wide table).
 *
 * BUGFIX (kept from the old app-wide version): category is deliberately
 * NOT a @PathVariable (e.g. no ".../store/{category}") on any of these -
 * categories are free text and can contain "/" (e.g. "Condiments/Sauces",
 * "Refrigerated/Frozen Goods"). Tomcat rejects encoded slashes ("%2F")
 * inside a URL PATH SEGMENT by default, so a category with a slash in its
 * name would 400 before even reaching this controller. Query params and
 * request bodies don't have this restriction, so category travels there
 * instead on every endpoint below. userId is numeric, so it's fine as a
 * path segment.
 */
@RestController
@RequestMapping("/api/users/{userId}/category-defaults")
public class CategoryDefaultController {

    private final CategoryDefaultService categoryDefaultService;

    public CategoryDefaultController(CategoryDefaultService categoryDefaultService) {
        this.categoryDefaultService = categoryDefaultService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDefaultDto>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(categoryDefaultService.getAll(userId));
    }

    @PutMapping("/store")
    public ResponseEntity<CategoryDefaultDto> setDefaultStore(@PathVariable Long userId, @RequestBody SetCategoryDefaultStoreRequest request) {
        return ResponseEntity.ok(categoryDefaultService.setDefaultStore(userId, request.getCategory(), request.getStoreId()));
    }

    @DeleteMapping("/store")
    public ResponseEntity<Void> clearDefaultStore(@PathVariable Long userId, @RequestParam String category) {
        categoryDefaultService.clearDefaultStore(userId, category);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ingredient")
    public ResponseEntity<CategoryDefaultDto> setDefaultIsIngredient(@PathVariable Long userId, @RequestBody SetCategoryDefaultIsIngredientRequest request) {
        return ResponseEntity.ok(categoryDefaultService.setDefaultIsIngredient(userId, request.getCategory(), request.isDefaultIsIngredient()));
    }
}
