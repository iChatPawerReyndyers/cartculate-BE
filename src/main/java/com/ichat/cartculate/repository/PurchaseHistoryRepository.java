package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    List<PurchaseHistory> findByUserId(Long userId);

    List<PurchaseHistory> findByUserIdAndPurchaseDateBetween(
        Long userId, LocalDateTime start, LocalDateTime end
    );
}
