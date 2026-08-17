package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateItemRequest;
import com.ichat.cartculate.dto.ItemDto;
import com.ichat.cartculate.dto.UpdateItemRequest;
import com.ichat.cartculate.entity.Item;
import com.ichat.cartculate.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Master item catalog, sorted by name - used by pickers like the New Recipe ingredient selector. */
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .sorted(Comparator.comparing(item -> item.getName().toLowerCase()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** POST /api/items - adds a new product to the master catalog, via the Price Catalog's "+ Add product" form. */
    public ItemDto createItem(CreateItemRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setUnit(request.getUnit());
        item.setIngredient(request.isIngredient());
        return toDto(itemRepository.save(item));
    }

    /** PUT /api/items/{itemId} - edits an existing product's name/category/unit, via the Price Catalog's edit modal. */
    public ItemDto updateItem(Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setUnit(request.getUnit());
        item.setIngredient(request.isIngredient());
        return toDto(itemRepository.save(item));
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(item.getId().toString(), item.getName(), item.getCategory(), item.getUnit(), item.isIngredient());
    }
}