package com.jankominek.disenchantment.stats;

import java.util.UUID;

/**
 * Immutable record of a single disenchantment or shatterment operation, written to the
 * SQLite database and used to update the in-memory {@link StatsCache}.
 */
public record OperationRecord(
        long timestamp,
        UUID playerUuid,
        String playerName,
        String world,
        OperationType operationType,
        String itemMaterial,
        String enchantmentKeys,
        int xpCost,
        double economyCost
) {
}
