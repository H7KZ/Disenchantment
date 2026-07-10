package com.jankominek.disenchantment.stats;

import java.util.Map;
import java.util.UUID;

public record BootData(
    long disenchants,
    long shatters,
    long xpSpent,
    double moneySpent,
    Map<String, Long> enchantmentCounts,
    Map<UUID, PlayerBootStats> playerStats
) {
    public record PlayerBootStats(
        String playerName,
        long disenchants,
        long shatters,
        long xpSpent,
        double moneySpent,
        long lastOperationEpoch
    ) {}
}
