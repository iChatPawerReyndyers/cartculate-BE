package com.ichat.cartculate.repository;

import com.ichat.cartculate.entity.UserCategoryDefault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCategoryDefaultRepository extends JpaRepository<UserCategoryDefault, Long> {
    List<UserCategoryDefault> findByUser_Id(Long userId);
    Optional<UserCategoryDefault> findByUser_IdAndCategory(Long userId, String category);
}