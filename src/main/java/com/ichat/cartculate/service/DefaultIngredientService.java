package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.DefaultIngredientDto;
import com.ichat.cartculate.entity.DefaultIngredient;
import com.ichat.cartculate.entity.Item;
import com.ichat.cartculate.entity.User;
import com.ichat.cartculate.repository.DefaultIngredientRepository;
import com.ichat.cartculate.repository.ItemRepository;
import com.ichat.cartculate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Per-user "Default ingredients" list (Pricing tab): products this user
 * wants automatically added as a row whenever THEY create a new recipe.
 * See DefaultIngredient's javadoc for where the list is actually applied
 * (client-side, when the new-recipe form opens).
 */
@Service
public class DefaultIngredientService {

    private final DefaultIngredientRepository defaultIngredientRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public DefaultIngredientService(
            DefaultIngredientRepository defaultIngredientRepository,
            ItemRepository itemRepository,
            UserRepository userRepository
    ) {
        this.defaultIngredientRepository = defaultIngredientRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    /** GET /api/users/{userId}/default-ingredients */
    public List<DefaultIngredientDto> getAll(Long userId) {
        return defaultIngredientRepository.findByUser_Id(userId).stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(dto -> dto.getItemName().toLowerCase()))
                .collect(Collectors.toList());
    }

    /** POST /api/users/{userId}/default-ingredients/{itemId} - adds a product to this user's default list. Already-listed is a no-op, not an error. */
    public DefaultIngredientDto add(Long userId, Long itemId) {
        return defaultIngredientRepository.findByUser_IdAndItem_Id(userId, itemId)
                .map(this::toDto)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
                    DefaultIngredient saved = defaultIngredientRepository.save(new DefaultIngredient(user, item));
                    return toDto(saved);
                });
    }

    /** DELETE /api/users/{userId}/default-ingredients/{itemId} - removes a product from this user's default list. Not-listed is a no-op, not an error. */
    public void remove(Long userId, Long itemId) {
        defaultIngredientRepository.findByUser_IdAndItem_Id(userId, itemId)
                .ifPresent(defaultIngredientRepository::delete);
    }

    private DefaultIngredientDto toDto(DefaultIngredient defaultIngredient) {
        Item item = defaultIngredient.getItem();
        return new DefaultIngredientDto(
                item.getId().toString(),
                item.getName(),
                item.getCategory(),
                item.getUnit()
        );
    }
}
