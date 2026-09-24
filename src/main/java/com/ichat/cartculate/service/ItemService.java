package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.ConvertItemUnitRequest;
import com.ichat.cartculate.dto.CreateItemRequest;
import com.ichat.cartculate.dto.ItemDto;
import com.ichat.cartculate.dto.ItemUnitUsageDto;
import com.ichat.cartculate.dto.UpdateItemRequest;
import com.ichat.cartculate.entity.Item;
import com.ichat.cartculate.entity.RecipeIngredient;
import com.ichat.cartculate.entity.StorePrice;
import com.ichat.cartculate.entity.UserCartItem;
import com.ichat.cartculate.entity.UserStorePrice;
import com.ichat.cartculate.entity.Store;
import com.ichat.cartculate.repository.CategoryDefaultRepository;
import com.ichat.cartculate.repository.ItemRepository;
import com.ichat.cartculate.repository.RecipeIngredientRepository;
import com.ichat.cartculate.repository.StorePriceRepository;
import com.ichat.cartculate.repository.UserCartItemRepository;
import com.ichat.cartculate.repository.UserStorePriceRepository;
import com.ichat.cartculate.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final StorePriceRepository storePriceRepository;
    private final UserStorePriceRepository userStorePriceRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final CategoryDefaultRepository categoryDefaultRepository;
    private final StoreRepository storeRepository;

    public ItemService(
            ItemRepository itemRepository,
            StorePriceRepository storePriceRepository,
            UserStorePriceRepository userStorePriceRepository,
            UserCartItemRepository userCartItemRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            CategoryDefaultRepository categoryDefaultRepository,
            StoreRepository storeRepository
    ) {
        this.itemRepository = itemRepository;
        this.storePriceRepository = storePriceRepository;
        this.userStorePriceRepository = userStorePriceRepository;
        this.userCartItemRepository = userCartItemRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.categoryDefaultRepository = categoryDefaultRepository;
        this.storeRepository = storeRepository;
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
        item.setDefaultStore(resolveDefaultStore(request.getDefaultStoreId(), request.getCategory()));
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
        item.setDefaultStore(request.getDefaultStoreId() == null ? null : storeRepository.findById(request.getDefaultStoreId())
            .orElseThrow(() -> new IllegalArgumentException("Store not found: " + request.getDefaultStoreId())));
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


    // ------------------------------------------------------------------
    // Unit conversion (e.g. a product priced per "kg" becomes "pc")
    // ------------------------------------------------------------------

    /**
     * GET /api/items/{itemId}/unit-usage - what a unit conversion of this
     * product would touch, for the app's "Convert unit" prompt preview.
     */
    @Transactional(readOnly = true)
    public ItemUnitUsageDto getUnitUsage(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        List<ItemUnitUsageDto.RecipeLine> recipeLines = recipeIngredientRepository.findByItem_Id(itemId).stream()
                .map(line -> {
                    BigDecimal multiplier = toItemUnitMultiplier(line.getUnit(), item.getUnit());
                    BigDecimal inItemUnit = multiplier == null ? null : line.getBaseQuantity().multiply(multiplier);
                    return new ItemUnitUsageDto.RecipeLine(
                            line.getRecipe().getRecipeName(), line.getBaseQuantity(), line.getUnit(), inItemUnit);
                })
                .sorted(Comparator.comparing(line -> line.getRecipeName().toLowerCase()))
                .collect(Collectors.toList());

        List<ItemUnitUsageDto.PriceLine> prices = storePriceRepository.findByItem_Id(itemId).stream()
                .map(price -> new ItemUnitUsageDto.PriceLine(price.getStore().getName(), price.getPriceAmount()))
                .sorted(Comparator.comparing(price -> price.getStoreName().toLowerCase()))
                .collect(Collectors.toList());

        int cartRows = userCartItemRepository.findByItem_Id(itemId).size();
        return new ItemUnitUsageDto(recipeLines, prices, cartRows);
    }

    /**
     * POST /api/items/{itemId}/convert-unit - switches a product to a new
     * unit AND rewrites everything that was expressed in the old one, so
     * costs and quantities stay correct:
     *
     *  - recipe ingredient lines: quantity x factor, unit = new unit. A line
     *    in a related unit (g for a kg product, mL for a L product, and the
     *    reverse) is converted through the old unit first. Lines in an
     *    unrelated unit (e.g. "pack") are left untouched.
     *  - shared store prices and personal price overrides: price / factor
     *    (80.00 per kg with 8 pc per kg -> 10.00 per pc).
     *  - cart rows (any user): quantity and pantry-override quantity x factor.
     *  - the product's own unit.
     *
     * Purchase history is deliberately not touched. @Transactional so a
     * failure part-way through rolls back everything instead of leaving
     * some recipes converted and others not.
     */
    @Transactional
    public ItemDto convertUnit(Long itemId, ConvertItemUnitRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        BigDecimal factor = request.getFactor();
        if (factor == null || factor.signum() <= 0 || factor.compareTo(MAX_CONVERSION_FACTOR) > 0) {
            throw new IllegalArgumentException("factor must be greater than 0 and at most " + MAX_CONVERSION_FACTOR);
        }
        String oldUnit = normalizeUnit(item.getUnit());
        String newUnit = normalizeUnit(request.getNewUnit());
        if (Objects.equals(oldUnit, newUnit)) {
            throw new IllegalArgumentException("The new unit is the same as the current unit");
        }

        for (RecipeIngredient line : recipeIngredientRepository.findByItem_Id(itemId)) {
            BigDecimal multiplier = toItemUnitMultiplier(line.getUnit(), oldUnit);
            if (multiplier == null) {
                continue; // unrelated unit - leave this line as the user wrote it
            }
            line.setBaseQuantity(line.getBaseQuantity().multiply(multiplier).multiply(factor)
                    .setScale(3, RoundingMode.HALF_UP));
            line.setUnit(newUnit);
        }

        for (StorePrice price : storePriceRepository.findByItem_Id(itemId)) {
            price.setPriceAmount(price.getPriceAmount().divide(factor, 2, RoundingMode.HALF_UP));
        }
        for (UserStorePrice price : userStorePriceRepository.findByItem_Id(itemId)) {
            price.setPriceAmount(price.getPriceAmount().divide(factor, 2, RoundingMode.HALF_UP));
        }

        for (UserCartItem row : userCartItemRepository.findByItem_Id(itemId)) {
            row.setQuantity(row.getQuantity().multiply(factor).setScale(3, RoundingMode.HALF_UP));
            row.setOverridePantryQty(row.getOverridePantryQty().multiply(factor).setScale(3, RoundingMode.HALF_UP));
        }

        item.setUnit(newUnit);
        return toDto(itemRepository.save(item));
    }

    private static final BigDecimal MAX_CONVERSION_FACTOR = new BigDecimal("100000");

    private static String normalizeUnit(String unit) {
        return unit == null || unit.isBlank() ? null : unit.trim();
    }

    /**
     * The number to multiply a recipe line's quantity by to express it in
     * the product's unit: 1 when the units match, 0.001 for g -> kg or
     * mL -> L, 1000 for kg -> g or L -> mL. Null when the two units aren't
     * related (so the line can't be converted).
     */
    private static BigDecimal toItemUnitMultiplier(String lineUnit, String itemUnit) {
        String line = normalizeUnit(lineUnit);
        String item = normalizeUnit(itemUnit);
        if (line == null && item == null) return BigDecimal.ONE;
        if (line == null || item == null) return null;
        line = line.toLowerCase();
        item = item.toLowerCase();
        if (line.equals(item)) return BigDecimal.ONE;
        if (line.equals("g") && item.equals("kg")) return new BigDecimal("0.001");
        if (line.equals("kg") && item.equals("g")) return new BigDecimal("1000");
        if (line.equals("ml") && item.equals("l")) return new BigDecimal("0.001");
        if (line.equals("l") && item.equals("ml")) return new BigDecimal("1000");
        return null;
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(item.getId().toString(), item.getName(), item.getCategory(), item.getUnit(), item.isIngredient(), item.isIncludeInCart(),
                item.getDefaultStore() == null ? null : item.getDefaultStore().getId().toString());
    }

    private Store resolveDefaultStore(Long requestedStoreId, String category) {
        if (requestedStoreId != null) {
            return storeRepository.findById(requestedStoreId)
                    .orElseThrow(() -> new IllegalArgumentException("Store not found: " + requestedStoreId));
        }
        return categoryDefaultRepository.findById(category)
                .map(com.ichat.cartculate.entity.CategoryDefault::getDefaultStore)
                .orElse(null);
    }
}