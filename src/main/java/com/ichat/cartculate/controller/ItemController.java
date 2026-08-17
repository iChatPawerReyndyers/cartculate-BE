package com.ichat.cartculate.controller;

import com.ichat.cartculate.dto.CreateItemRequest;
import com.ichat.cartculate.dto.ItemDto;
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
}