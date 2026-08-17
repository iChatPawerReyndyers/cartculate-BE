package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    /** Used by store creation to avoid creating a duplicate when a store with the same name (any case) already exists. */
    Optional<Store> findByNameIgnoreCase(String name);
}
