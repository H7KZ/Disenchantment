package com.jankominek.disenchantment.stats;

import java.util.Map;
import java.util.UUID;

/**
 * Snapshot of aggregate statistics loaded from the database at plugin startup,
 * used to seed the in-memory {@link StatsCache}.
 */
public record BootData(
        long disenchants,
        long shatters,
        long xpSpent,
        double moneySpent,
        Map<String, Long> enchantmentCounts,
        Map<UUID, PlayerBootStats> playerStats
) {
    /**
     * Per-player aggregate statistics loaded at boot.
     */
    public record PlayerBootStats(
            String playerName,
            long disenchants,
            long shatters,
            long xpSpent,
            double moneySpent,
            long lastOperationEpoch
    ) {
    }
}
