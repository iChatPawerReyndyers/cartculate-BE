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

    /** Used by "Done Checkout" reconciliation: the checked-off rows for one store's trip. */
    List<UserCartItem> findByUserIdAndStoreIdAndIsCheckedCheckoutTrue(Long userId, Long storeId);

    /** Used by recipe-multiplier sync: the cart row (if any) already sourced from this recipe for this item+store. */
    List<UserCartItem> findByUserIdAndItemIdAndStoreIdAndSourceRecipeId(
            Long userId, Long itemId, Long storeId, Long sourceRecipeId
    );

    /** Used by "move item to a different store": every row for this item at this store, regardless of source (recipe-sourced or manual "Others") - a single consolidated card can be backed by more than one underlying row. */
    List<UserCartItem> findByUserIdAndItemIdAndStoreId(Long userId, Long itemId, Long storeId);

    /** Used when a recipe is deleted or a checkout trip completes with unchecked items: every row sourced from this recipe. */
    List<UserCartItem> findBySourceRecipeId(Long sourceRecipeId);

    /** Used when deleting a product entirely - every cart row (any user, any store, any source) referencing that item. */
    List<UserCartItem> findByItem_Id(Long itemId);
}