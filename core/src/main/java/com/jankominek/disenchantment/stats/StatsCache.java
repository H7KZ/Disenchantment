package com.jankominek.disenchantment.stats;

import java.time.Instant;
import java.util.*;

/**
 * In-memory aggregate of all recorded operations.
 * All mutation happens on the Bukkit main thread (Post events fire on main thread),
 * so no synchronisation is needed for writes. Reads from async threads (PAPI, bStats)
 * see eventually-consistent data, which is acceptable for display purposes.
 */
public class StatsCache {

    public record PlayerStats(
            long disenchants,
            long shatters,
            long xpSpent,
            double moneySpent,
            Instant lastOperation
    ) {
        PlayerStats add(OperationType type, int xpCost, double economyCost, Instant time) {
            return new PlayerStats(
                    disenchants + (type == OperationType.DISENCHANT ? 1 : 0),
                    shatters + (type == OperationType.SHATTER ? 1 : 0),
                    xpSpent + xpCost,
                    moneySpent + economyCost,
                    time
            );
        }
    }

    private long totalDisenchants;
    private long totalShatters;
    private long totalXpSpent;
    private double totalMoneySpent;
    private final Map<String, Long> enchantmentCounts = new HashMap<>();
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();

    /** Populates cache from a DB boot-load snapshot. Call once before any record() calls. */
    public void initialize(BootData bd) {
        totalDisenchants = bd.disenchants();
        totalShatters = bd.shatters();
        totalXpSpent = bd.xpSpent();
        totalMoneySpent = bd.moneySpent();
        enchantmentCounts.putAll(bd.enchantmentCounts());
        bd.playerStats().forEach((uuid, pbs) -> playerStats.put(uuid,
                new PlayerStats(pbs.disenchants(), pbs.shatters(), pbs.xpSpent(),
                        pbs.moneySpent(), Instant.ofEpochSecond(pbs.lastOperationEpoch()))));
    }

    /** Records one operation in the cache. Call on main thread. */
    public void record(OperationType type, List<String> enchantmentKeys,
                       UUID playerUuid, String playerName, int xpCost, double economyCost, Instant timestamp) {
        if (type == OperationType.DISENCHANT) totalDisenchants++;
        else totalShatters++;
        totalXpSpent += xpCost;
        totalMoneySpent += economyCost;
        for (String key : enchantmentKeys) {
            enchantmentCounts.merge(key, 1L, Long::sum);
        }
        playerStats.merge(playerUuid,
                new PlayerStats(
                        type == OperationType.DISENCHANT ? 1 : 0,
                        type == OperationType.SHATTER ? 1 : 0,
                        xpCost, economyCost, timestamp),
                (existing, n) -> existing.add(type, xpCost, economyCost, timestamp));
    }

    public long getTotalDisenchants() { return totalDisenchants; }
    public long getTotalShatters() { return totalShatters; }
    public long getTotalXpSpent() { return totalXpSpent; }
    public double getTotalMoneySpent() { return totalMoneySpent; }

    public long getEnchantmentCount(String key) {
        return enchantmentCounts.getOrDefault(key, 0L);
    }

    public Map<String, Long> getEnchantmentCounts() {
        return Collections.unmodifiableMap(enchantmentCounts);
    }

    public StatsCache.PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    public Map<UUID, PlayerStats> getAllPlayerStats() {
        return Collections.unmodifiableMap(playerStats);
    }

    /** Returns the enchantment key with the highest operation count, or "none" if empty. */
    public String getTopEnchantment() {
        return enchantmentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
    }
}
