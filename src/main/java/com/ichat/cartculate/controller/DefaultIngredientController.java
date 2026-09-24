package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.DefaultIngredientDto;
import com.ichat.cartculate.service.DefaultIngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Per-user (see DefaultIngredient's javadoc). */
@RestController
@RequestMapping("/api/users/{userId}/default-ingredients")
public class DefaultIngredientController {

    private final DefaultIngredientService defaultIngredientService;

    public DefaultIngredientController(DefaultIngredientService defaultIngredientService) {
        this.defaultIngredientService = defaultIngredientService;
    }

    @GetMapping
    public ResponseEntity<List<DefaultIngredientDto>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(defaultIngredientService.getAll(userId));
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<DefaultIngredientDto> add(@PathVariable Long userId, @PathVariable Long itemId) {
        return ResponseEntity.ok(defaultIngredientService.add(userId, itemId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> remove(@PathVariable Long userId, @PathVariable Long itemId) {
        defaultIngredientService.remove(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}
