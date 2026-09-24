package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CategoryDefaultDto;
import com.ichat.cartculate.entity.Store;
import com.ichat.cartculate.entity.User;
import com.ichat.cartculate.entity.UserCategoryDefault;
import com.ichat.cartculate.repository.StoreRepository;
import com.ichat.cartculate.repository.UserCategoryDefaultRepository;
import com.ichat.cartculate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Per-user category defaults (Pricing tab's "Category defaults" card). See UserCategoryDefault's javadoc. */
@Service
public class CategoryDefaultService {

    private final UserCategoryDefaultRepository userCategoryDefaultRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public CategoryDefaultService(
            UserCategoryDefaultRepository userCategoryDefaultRepository,
            StoreRepository storeRepository,
            UserRepository userRepository
    ) {
        this.userCategoryDefaultRepository = userCategoryDefaultRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/users/{userId}/category-defaults - one entry per category
     * THIS USER has any setting configured for (a default store,
     * defaultIsIngredient=true, or both). A category with nothing set for
     * this user simply has no row (see the two setters below), so there's
     * nothing to filter out here.
     */
    public List<CategoryDefaultDto> getAll(Long userId) {
        return userCategoryDefaultRepository.findByUser_Id(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** PUT /api/users/{userId}/category-defaults/store - sets/changes this user's default store for a category. Only affects NEW products created afterward. */
    public CategoryDefaultDto setDefaultStore(Long userId, String category, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
        UserCategoryDefault entry = findOrCreate(userId, category);
        entry.setDefaultStore(store);
        return toDto(userCategoryDefaultRepository.save(entry));
    }

    /**
     * DELETE /api/users/{userId}/category-defaults/store?category=... -
     * clears just this user's store default ("None set"). If
     * defaultIsIngredient is also false afterward, the row is deleted
     * entirely rather than left sitting at all-defaults.
     */
    public void clearDefaultStore(Long userId, String category) {
        userCategoryDefaultRepository.findByUser_IdAndCategory(userId, category).ifPresent(existing -> {
            existing.setDefaultStore(null);
            if (!existing.isDefaultIsIngredient()) {
                userCategoryDefaultRepository.delete(existing);
            } else {
                userCategoryDefaultRepository.save(existing);
            }
        });
    }

    /** PUT /api/users/{userId}/category-defaults/ingredient - sets whether new products this user creates in this category default to "Ingredient" on. */
    public CategoryDefaultDto setDefaultIsIngredient(Long userId, String category, boolean defaultIsIngredient) {
        UserCategoryDefault entry = findOrCreate(userId, category);
        entry.setDefaultIsIngredient(defaultIsIngredient);
        if (!defaultIsIngredient && entry.getDefaultStore() == null) {
            userCategoryDefaultRepository.delete(entry);
            return new CategoryDefaultDto(category, null, null, false);
        }
        return toDto(userCategoryDefaultRepository.save(entry));
    }

    private UserCategoryDefault findOrCreate(Long userId, String category) {
        return userCategoryDefaultRepository.findByUser_IdAndCategory(userId, category)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                    return new UserCategoryDefault(user, category, null, false);
                });
    }

    private CategoryDefaultDto toDto(UserCategoryDefault entry) {
        Store store = entry.getDefaultStore();
        return new CategoryDefaultDto(
                entry.getCategory(),
                store != null ? store.getId().toString() : null,
                store != null ? store.getName() : null,
                entry.isDefaultIsIngredient()
        );
    }
}
