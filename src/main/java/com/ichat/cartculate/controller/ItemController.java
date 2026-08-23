package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreateItemRequest;
import com.ichat.cartculate.dto.ItemDto;
import com.ichat.cartculate.dto.UpdateIncludeInCartRequest;
import com.ichat.cartculate.dto.UpdateItemRequest;
import com.ichat.cartculate.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /** GET /api/items - the master item catalog, used by pickers like the New Recipe ingredient selector. */
    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    /** POST /api/items - adds a new product, via the Price Catalog's "+ Add product" form. */
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody CreateItemRequest request) {
        ItemDto created = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/items/{itemId} - edits an existing product, via the Price Catalog's edit modal. */
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest request
    ) {
        return ResponseEntity.ok(itemService.updateItem(itemId, request));
    }

    /** PATCH /api/items/{itemId}/include-in-cart - the Price Catalog checkbox controlling Cart tab visibility. */
    @PatchMapping("/{itemId}/include-in-cart")
    public ResponseEntity<ItemDto> updateIncludeInCart(
            @PathVariable Long itemId,
            @RequestBody UpdateIncludeInCartRequest request
    ) {
        return ResponseEntity.ok(itemService.updateIncludeInCart(itemId, request.isIncludeInCart()));
    }

    /** DELETE /api/items/{itemId} - removes a product entirely, via the Price Catalog's delete action. Also removes its prices, cart rows, and recipe ingredient lines - see ItemService.deleteItem's javadoc. */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}