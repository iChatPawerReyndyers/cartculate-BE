package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CategoryDefaultDto;
import com.ichat.cartculate.dto.SetCategoryDefaultIsIngredientRequest;
import com.ichat.cartculate.dto.SetCategoryDefaultStoreRequest;
import com.ichat.cartculate.service.CategoryDefaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BUGFIX: category is deliberately NOT a @PathVariable (e.g. no
 * "/{category}/store") on any of these - categories are free text and
 * can contain "/" (e.g. "Condiments/Sauces", "Refrigerated/Frozen
 * Goods"). Tomcat rejects encoded slashes ("%2F") inside a URL PATH
 * SEGMENT by default (a security setting, ALLOW_ENCODED_SLASH=false),
 * so a category with a slash in its name would 400 before even reaching
 * this controller - silently, since the frontend's generic error handler
 * just shows "could not update" either way. Query params and request
 * bodies don't have this restriction, so category travels there instead
 * on every endpoint below.
 */
@RestController
@RequestMapping("/api/category-defaults")
public class CategoryDefaultController {

    private final CategoryDefaultService categoryDefaultService;

    public CategoryDefaultController(CategoryDefaultService categoryDefaultService) {
        this.categoryDefaultService = categoryDefaultService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDefaultDto>> getAll() {
        return ResponseEntity.ok(categoryDefaultService.getAll());
    }

    @PutMapping("/store")
    public ResponseEntity<CategoryDefaultDto> setDefaultStore(@RequestBody SetCategoryDefaultStoreRequest request) {
        return ResponseEntity.ok(categoryDefaultService.setDefaultStore(request.getCategory(), request.getStoreId()));
    }

    @DeleteMapping("/store")
    public ResponseEntity<Void> clearDefaultStore(@RequestParam String category) {
        categoryDefaultService.clearDefaultStore(category);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ingredient")
    public ResponseEntity<CategoryDefaultDto> setDefaultIsIngredient(@RequestBody SetCategoryDefaultIsIngredientRequest request) {
        return ResponseEntity.ok(categoryDefaultService.setDefaultIsIngredient(request.getCategory(), request.isDefaultIsIngredient()));
    }
}
