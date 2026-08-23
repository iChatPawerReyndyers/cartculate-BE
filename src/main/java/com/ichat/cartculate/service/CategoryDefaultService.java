package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CategoryDefaultDto;
import com.ichat.cartculate.entity.CategoryDefault;
import com.ichat.cartculate.entity.Store;
import com.ichat.cartculate.repository.CategoryDefaultRepository;
import com.ichat.cartculate.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryDefaultService {

    private final CategoryDefaultRepository categoryDefaultRepository;
    private final StoreRepository storeRepository;

    public CategoryDefaultService(CategoryDefaultRepository categoryDefaultRepository, StoreRepository storeRepository) {
        this.categoryDefaultRepository = categoryDefaultRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * GET /api/category-defaults - one entry per category that has ANY
     * setting configured (a default store, defaultIsIngredient=true, or
     * both). A category with nothing set simply never gets a row created
     * in the first place (see the two setters below), so there's nothing
     * to filter out here.
     */
    public List<CategoryDefaultDto> getAll() {
        return categoryDefaultRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** PUT /api/category-defaults/{category}/store - sets/changes a category's default store. Only affects NEW products created afterward - see CategoryDefault.java's javadoc. */
    public CategoryDefaultDto setDefaultStore(String category, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
        CategoryDefault categoryDefault = categoryDefaultRepository.findById(category)
                .orElseGet(() -> new CategoryDefault(category, null, false));
        categoryDefault.setDefaultStore(store);
        return toDto(categoryDefaultRepository.save(categoryDefault));
    }

    /**
     * DELETE /api/category-defaults/{category}/store - clears just the
     * store default ("None set"). If defaultIsIngredient is also false
     * afterward, the row is deleted entirely rather than left sitting at
     * all-defaults, so it doesn't show up in getAll() (matching the
     * "category with nothing set has no row" contract above).
     */
    public void clearDefaultStore(String category) {
        categoryDefaultRepository.findById(category).ifPresent(existing -> {
            existing.setDefaultStore(null);
            if (!existing.isDefaultIsIngredient()) {
                categoryDefaultRepository.delete(existing);
            } else {
                categoryDefaultRepository.save(existing);
            }
        });
    }

    /** PUT /api/category-defaults/{category}/ingredient - sets whether new products in this category default to "Ingredient" on. */
    public CategoryDefaultDto setDefaultIsIngredient(String category, boolean defaultIsIngredient) {
        CategoryDefault categoryDefault = categoryDefaultRepository.findById(category)
                .orElseGet(() -> new CategoryDefault(category, null, false));
        categoryDefault.setDefaultIsIngredient(defaultIsIngredient);
        // Same "delete if back to all-defaults" tidiness as clearDefaultStore, mirrored for this direction.
        if (!defaultIsIngredient && categoryDefault.getDefaultStore() == null) {
            categoryDefaultRepository.delete(categoryDefault);
            return new CategoryDefaultDto(category, null, null, false);
        }
        return toDto(categoryDefaultRepository.save(categoryDefault));
    }

    private CategoryDefaultDto toDto(CategoryDefault categoryDefault) {
        Store store = categoryDefault.getDefaultStore();
        return new CategoryDefaultDto(
                categoryDefault.getCategory(),
                store != null ? store.getId().toString() : null,
                store != null ? store.getName() : null,
                categoryDefault.isDefaultIsIngredient()
        );
    }
}