package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One archived RECEIPT (not one archived line item, per the updated spec).
 * Written at Checkout completion once the user manually enters the total
 * receipt amount. itemManifestJson is a compressed audit snapshot of what
 * was checked off, for later re-display/dispute - it is NOT a normalized
 * per-item price log, which is a deliberate tradeoff in the updated spec.
 *
 * itemManifestJson expected shape (a JSON array), so it stays useful for
 * future analytics rebuilds:
 *   [{ "itemId": 6, "itemName": "Napkin", "category": "Toiletries",
 *      "quantity": 2, "pricePerUnit": 25.00 }, ...]
 */
@Entity
@Table(name = "purchase_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Manually entered receipt total at Checkout completion. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalReceiptSpent;

    @Column(nullable = false)
    private LocalDateTime purchaseDate = LocalDateTime.now();

    /**
     * Compressed audit snapshot of checked item strings & quantities.
     * Stored as jsonb for native Postgres JSON querying if ever needed;
     * treated as an opaque string on the Java side to avoid pulling in a
     * JSON-mapping library for what's meant to be a write-mostly log.
     *
     * @JdbcTypeCode(SqlTypes.JSON) is the actual fix here, not just
     * columnDefinition="jsonb" below - columnDefinition only affects
     * ddl-auto's generated CREATE/ALTER TABLE statement, it does NOT tell
     * Hibernate how to bind this parameter on INSERT/UPDATE. Without it,
     * Hibernate sends the String as a plain VARCHAR bind parameter, and
     * Postgres rejects the implicit VARCHAR-to-jsonb cast on a prepared
     * statement (this is the exact "column is of type jsonb but
     * expression is of type character varying" error). JdbcTypeCode tells
     * Hibernate to bind it as actual JSON, matching the column's real type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String itemManifestJson;
}