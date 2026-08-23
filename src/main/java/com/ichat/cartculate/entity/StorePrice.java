package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Composite-key mapping of one item's price at one store, e.g.
 * Napkin @ Puregold != Napkin @ S&R. Written to by the receipt scanner's
 * "Confirm" action and by manual per-store price entry.
 */
@Entity
@Table(name = "store_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorePrice {

    @EmbeddedId
    private StorePriceId id;

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

    /**
     * Feature: tag each price with where it came from (receipt scan
     * confirm vs manual entry in the Pricing tab), so the catalog can
     * show which is which. Defaults MANUAL - columnDefinition gives
     * Hibernate's ddl-auto=update a real DB-level default so adding this
     * NOT NULL column doesn't fail against an already-populated
     * store_prices table, same reasoning as Item.includeInCart.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'MANUAL'")
    private PriceSource priceSource = PriceSource.MANUAL;
}
