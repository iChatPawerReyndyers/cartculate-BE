package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
