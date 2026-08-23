package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.UserStorePrice;
import com.ichat.cartculate.entity.UserStorePriceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStorePriceRepository extends JpaRepository<UserStorePrice, UserStorePriceId> {
    List<UserStorePrice> findByUser_Id(Long userId);

    Optional<UserStorePrice> findByUser_IdAndItem_IdAndStore_Id(Long userId, Long itemId, Long storeId);

    /** Used when deleting a product entirely (ItemService.deleteItem) - every personal override (any user) referencing that item. */
    List<UserStorePrice> findByItem_Id(Long itemId);
}
