package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Feature: personal price overrides ("shared baseline + personal
 * override" model). StorePrice (a separate table, unchanged) remains the
 * shared/community price everyone sees by default - this table holds the
 * exceptions: a specific user's own price for a specific item at a
 * specific store, e.g. because their suki at a wet market stall gives
 * them a different deal than the "official" price. When resolving what
 * price to show a user, a row here always wins over StorePrice for that
 * exact (user, item, store) combination; StorePriceService.getResolvedPricesForUser
 * does that merge. Deliberately a separate table rather than adding a
 * nullable user_id onto StorePrice itself - that would make "shared vs
 * personal" ambiguous per-row (is a null user_id "shared" or "nobody's
 * set it yet"?) and would require every existing StorePrice consumer to
 * add user-filtering logic. Keeping them separate means StorePrice stays
 * exactly as simple as it was before this feature.
 */
@Entity
@Table(name = "user_store_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStorePrice {

    @EmbeddedId
    private UserStorePriceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAmount;

    /** Same tagging as StorePrice.priceSource - a scanned receipt sets this to SCAN, manual entry to MANUAL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'MANUAL'")
    private PriceSource priceSource = PriceSource.MANUAL;
}
