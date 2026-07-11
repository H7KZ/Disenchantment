package com.jankominek.disenchantment.utils;

import org.bukkit.entity.Player;

/**
 * A pluggable source of per-player cooldown state for anvil operations.
 * Implementations may track cooldowns purely in-memory, or delegate to a
 * third-party plugin's own cooldown system.
 */
public interface CooldownDelegate {
    /**
     * Returns whether the player is currently on cooldown.
     */
    boolean isOnCooldown(Player player);

    /**
     * Returns the number of whole seconds remaining before the player's
     * cooldown expires, or 0 if not on cooldown.
     */
    long getRemainingSeconds(Player player);

    /**
     * Records that the player just performed an operation, starting a new cooldown.
     */
    void recordOperation(Player player);
}
