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
}
