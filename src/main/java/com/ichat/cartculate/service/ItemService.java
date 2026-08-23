package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateItemRequest;
import com.ichat.cartculate.dto.ItemDto;
import com.ichat.cartculate.dto.UpdateItemRequest;
import com.ichat.cartculate.entity.Item;
import com.ichat.cartculate.repository.ItemRepository;
import com.ichat.cartculate.repository.RecipeIngredientRepository;
import com.ichat.cartculate.repository.StorePriceRepository;
import com.ichat.cartculate.repository.UserCartItemRepository;
import com.ichat.cartculate.repository.UserStorePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final StorePriceRepository storePriceRepository;
    private final UserStorePriceRepository userStorePriceRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public ItemService(
            ItemRepository itemRepository,
            StorePriceRepository storePriceRepository,
            UserStorePriceRepository userStorePriceRepository,
            UserCartItemRepository userCartItemRepository,
            RecipeIngredientRepository recipeIngredientRepository
    ) {
        this.itemRepository = itemRepository;
        this.storePriceRepository = storePriceRepository;
        this.userStorePriceRepository = userStorePriceRepository;
        this.userCartItemRepository = userCartItemRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
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

    /** PATCH /api/items/{itemId}/include-in-cart - toggles the Price Catalog checkbox controlling Cart tab visibility. */
    public ItemDto updateIncludeInCart(Long itemId, boolean includeInCart) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        item.setIncludeInCart(includeInCart);
        return toDto(itemRepository.save(item));
    }

    /**
     * DELETE /api/items/{itemId} - removes a product entirely, via the
     * Price Catalog's delete action. Items are referenced by four other
     * tables (store_prices, user_store_prices, user_cart_item,
     * recipe_ingredients), none of
     * which cascade automatically at the DB level, so a plain
     * itemRepository.delete() would fail with a foreign key violation the
     * moment the item has a price, is in someone's cart, or is used in a
     * recipe - which in practice is almost always. Rather than blocking
     * deletion until the user manually untangles all four first (a bad
     * experience for what should be a simple "remove this product"
     * action), this explicitly deletes the dependent rows first: its
     * shared prices at every store, every user's personal price override
     * for it, every cart row referencing it (any user, any source), and
     * every recipe ingredient line using it. @Transactional so a failure
     * partway through rolls back everything instead of leaving the item
     * half-deleted.
     */
    @Transactional
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        storePriceRepository.deleteAll(storePriceRepository.findByItem_Id(itemId));
        userStorePriceRepository.deleteAll(userStorePriceRepository.findByItem_Id(itemId));
        userCartItemRepository.deleteAll(userCartItemRepository.findByItem_Id(itemId));
        recipeIngredientRepository.deleteAll(recipeIngredientRepository.findByItem_Id(itemId));
        itemRepository.deleteById(itemId);
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(item.getId().toString(), item.getName(), item.getCategory(), item.getUnit(), item.isIngredient(), item.isIncludeInCart());
    }
}