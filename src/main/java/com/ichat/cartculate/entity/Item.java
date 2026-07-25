package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Master item list. e.g. "Carrots" (category "Vegetables"). */
@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** e.g. Produce, Meat, Household. Used by the Insights category pie chart. */
    @Column(nullable = false)
    private String category;
}
