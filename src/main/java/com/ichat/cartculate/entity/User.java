package com.ichat.cartculate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** Persisted Home/Away toggle for the Cart screen, per Feature 1. Defaults to HOME. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserMode currentMode = UserMode.HOME;
}