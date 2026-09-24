package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.DefaultIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DefaultIngredientRepository extends JpaRepository<DefaultIngredient, Long> {
    List<DefaultIngredient> findByUser_Id(Long userId);
    Optional<DefaultIngredient> findByUser_IdAndItem_Id(Long userId, Long itemId);
}
