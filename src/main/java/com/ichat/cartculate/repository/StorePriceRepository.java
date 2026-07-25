package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.StorePrice;
import com.ichat.cartculate.entity.StorePriceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorePriceRepository extends JpaRepository<StorePrice, StorePriceId> {
    List<StorePrice> findByStoreId(Long storeId);
}
