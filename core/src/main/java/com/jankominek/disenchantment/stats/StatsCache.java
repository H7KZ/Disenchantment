package com.jankominek.disenchantment.stats;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * In-memory aggregate of all recorded operations.
 * Thread-safe: on Folia writes may come from several region threads at once while reads come from
 * async threads (PAPI, bStats). Totals use {@link LongAdder}/{@link DoubleAdder} and the maps are
 * {@link ConcurrentHashMap}s updated through atomic {@code merge} calls.
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
                    lastOperation == null || time.isAfter(lastOperation) ? time : lastOperation
            );
        }
    }

    private final LongAdder totalDisenchants = new LongAdder();
    private final LongAdder totalShatters = new LongAdder();
    private final LongAdder totalXpSpent = new LongAdder();
    private final DoubleAdder totalMoneySpent = new DoubleAdder();
    private final Map<String, Long> enchantmentCounts = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerStats> playerStats = new ConcurrentHashMap<>();

    /**
     * Populates cache from a DB boot-load snapshot. Call once. Values are added to whatever
     * has already been recorded, so a record that races the boot load is never lost.
     */
    public void initialize(BootData bd) {
        totalDisenchants.add(bd.disenchants());
        totalShatters.add(bd.shatters());
        totalXpSpent.add(bd.xpSpent());
        totalMoneySpent.add(bd.moneySpent());
        bd.enchantmentCounts().forEach((key, count) -> enchantmentCounts.merge(key, count, Long::sum));
        bd.playerStats().forEach((uuid, pbs) -> playerStats.merge(uuid,
                new PlayerStats(pbs.disenchants(), pbs.shatters(), pbs.xpSpent(),
                        pbs.moneySpent(), Instant.ofEpochSecond(pbs.lastOperationEpoch())),
                (existing, boot) -> new PlayerStats(
                        existing.disenchants() + boot.disenchants(),
                        existing.shatters() + boot.shatters(),
                        existing.xpSpent() + boot.xpSpent(),
                        existing.moneySpent() + boot.moneySpent(),
                        existing.lastOperation().isAfter(boot.lastOperation()) ? existing.lastOperation() : boot.lastOperation())));
    }

    /**
     * Records one operation in the cache. Safe to call from any thread.
     */
    public void record(OperationType type, List<String> enchantmentKeys,
                       UUID playerUuid, String playerName, int xpCost, double economyCost, Instant timestamp) {
        if (type == OperationType.DISENCHANT) totalDisenchants.increment();
        else if (type == OperationType.SHATTER) totalShatters.increment();
        totalXpSpent.add(xpCost);
        totalMoneySpent.add(economyCost);
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
        return totalDisenchants.sum();
    }

    /**
     * Returns the total number of shatterment operations recorded.
     */
    public long getTotalShatters() {
        return totalShatters.sum();
    }

    /**
     * Returns the total XP levels spent across all operations.
     */
    public long getTotalXpSpent() {
        return totalXpSpent.sum();
    }

    /**
     * Returns the total economy currency spent across all operations.
     */
    public double getTotalMoneySpent() {
        return totalMoneySpent.sum();
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
