package com.jankominek.disenchantment.stats;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory aggregate of all recorded operations.
 * Writes happen on the Bukkit main thread; reads come from async threads (PAPI, bStats).
 * ConcurrentHashMap avoids ConcurrentModificationException on concurrent iterator/stream use.
 */
public class StatsCache {

    /**
     * Immutable per-player aggregate statistics held in the cache.
     */
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
    private final Map<String, Long> enchantmentCounts = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();

    /**
     * Populates cache from a DB boot-load snapshot. Call once before any record() calls.
     */
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

    /**
     * Records one operation in the cache. Call on main thread.
     */
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

    /**
     * Returns the total number of disenchantment operations recorded.
     */
    public long getTotalDisenchants() {
        return totalDisenchants;
    }

    /**
     * Returns the total number of shatterment operations recorded.
     */
    public long getTotalShatters() {
        return totalShatters;
    }

    /**
     * Returns the total XP levels spent across all operations.
     */
    public long getTotalXpSpent() {
        return totalXpSpent;
    }

    /**
     * Returns the total economy currency spent across all operations.
     */
    public double getTotalMoneySpent() {
        return totalMoneySpent;
    }

    /**
     * Returns the number of operations recorded for the given enchantment key, or 0 if unknown.
     *
     * @param key the enchantment key (e.g. {@code "mending"})
     */
    public long getEnchantmentCount(String key) {
        return enchantmentCounts.getOrDefault(key, 0L);
    }

    /**
     * Returns an unmodifiable view of the enchantment operation counts.
     */
    public Map<String, Long> getEnchantmentCounts() {
        return Collections.unmodifiableMap(enchantmentCounts);
    }

    /**
     * Returns the cached stats for the given player, or {@code null} if no data exists yet.
     *
     * @param uuid the player's UUID
     */
    public StatsCache.PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.get(uuid);
    }

    /**
     * Returns an unmodifiable view of all per-player stats in the cache.
     */
    public Map<UUID, PlayerStats> getAllPlayerStats() {
        return Collections.unmodifiableMap(playerStats);
    }

    /**
     * Returns the enchantment key with the highest operation count, or "none" if empty.
     */
    public String getTopEnchantment() {
        return enchantmentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
    }
}
