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
        Map<UUID, PlayerBootStats> playerStats,
        long grindExtracts,
        long grindTransfers
) {
    /**
     * Compatibility constructor for callers that predate the grindstone counters.
     */
    public BootData(long disenchants, long shatters, long xpSpent, double moneySpent,
                    Map<String, Long> enchantmentCounts, Map<UUID, PlayerBootStats> playerStats) {
        this(disenchants, shatters, xpSpent, moneySpent, enchantmentCounts, playerStats, 0L, 0L);
    }

    /**
     * Per-player aggregate statistics loaded at boot.
     */
    public record PlayerBootStats(
            String playerName,
            long disenchants,
            long shatters,
            long xpSpent,
            double moneySpent,
            long lastOperationEpoch,
            long grindstones
    ) {
        /**
         * Compatibility constructor for callers that predate the grindstone counter.
         */
        public PlayerBootStats(String playerName, long disenchants, long shatters, long xpSpent, double moneySpent, long lastOperationEpoch) {
            this(playerName, disenchants, shatters, xpSpent, moneySpent, lastOperationEpoch, 0L);
        }
    }
}
