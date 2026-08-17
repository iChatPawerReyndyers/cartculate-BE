package com.ichat.cartculate.service;

import com.ichat.cartculate.dto.CreateStoreRequest;
import com.ichat.cartculate.dto.StoreDto;
import com.ichat.cartculate.entity.Store;
import com.ichat.cartculate.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreDto> getAllStores() {
        return storeRepository.findAll().stream()
                .sorted(Comparator.comparing(store -> store.getName().toLowerCase()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * POST /api/stores - lets the Pricing tab's "+ Add new store" option
     * create a store on the fly instead of requiring it to already exist.
     * Reuses an existing store (case-insensitive name match) rather than
     * creating a duplicate if the name already exists - e.g. typing
     * "puregold" when "Puregold" is already saved just returns that one.
     */
    public StoreDto createStore(CreateStoreRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Store name is required");
        }

        Store existing = storeRepository.findByNameIgnoreCase(name).orElse(null);
        if (existing != null) {
            return toDto(existing);
        }

        Store store = new Store();
        store.setName(name);
        return toDto(storeRepository.save(store));
    }

    private StoreDto toDto(Store store) {
        return new StoreDto(store.getId().toString(), store.getName());
    }
}
