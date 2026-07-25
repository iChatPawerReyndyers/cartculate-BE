package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.UserCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCartItemRepository extends JpaRepository<UserCartItem, Long> {
    List<UserCartItem> findByUserId(Long userId);

    /** Used when applying a +/- tap: finds the manual "Others" row (sourceRecipe is null). */
    List<UserCartItem> findByUserIdAndItemIdAndStoreIdAndSourceRecipeIsNull(
        Long userId, Long itemId, Long storeId
    );
}
