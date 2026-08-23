package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.StorePrice;
import com.ichat.cartculate.entity.StorePriceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorePriceRepository extends JpaRepository<StorePrice, StorePriceId> {
    List<StorePrice> findByStoreId(Long storeId);

    /** Used when deleting a product entirely - every price row for that item, across all stores. */
    List<StorePrice> findByItem_Id(Long itemId);
}
