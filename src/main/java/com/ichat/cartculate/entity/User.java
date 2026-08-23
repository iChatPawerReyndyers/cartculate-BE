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

    /**
     * Login identifier, distinct from email per the "username and password
     * only" login screen requirement. Nullable at the JPA/entity level
     * purely so ddl-auto=update can add this column to an existing
     * non-empty users table without Postgres rejecting the ALTER TABLE
     * (a brand-new NOT NULL column against existing rows fails otherwise)
     * - AuthService still enforces "required" at the application layer for
     * every row created through /api/auth/signup from here on.
     */
    @Column(unique = true)
    private String username;

    /** BCrypt hash only - see AuthService, never the raw password. Nullable for the same ddl-auto=update reason as username above. */
    @Column(name = "password_hash")
    private String passwordHash;

    /** Persisted Home/Away toggle for the Cart screen, per Feature 1. Defaults to HOME. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserMode currentMode = UserMode.HOME;
}