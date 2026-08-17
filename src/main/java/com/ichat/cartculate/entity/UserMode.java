package com.ichat.cartculate.entity;

/**
 * Which mode the user's Cart screen is currently in:
 * HOME = editing pantry overrides (what's already stocked at home).
 * AWAY = mid grocery-trip, checking items off via "Start Grocery" mode.
 */
public enum UserMode {
    HOME,
    AWAY
}