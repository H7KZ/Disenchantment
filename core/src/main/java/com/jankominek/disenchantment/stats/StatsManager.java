package com.jankominek.disenchantment.stats;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.utils.BStatsMetrics;
import com.jankominek.disenchantment.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Nullable singleton that coordinates stats recording between the in-memory {@link StatsCache}
 * and the persistent {@link StatsDatabase}. {@link #getInstance()} returns {@code null} when
 * {@code logging.operations} is disabled; all call sites must null-check.
 */
public class StatsManager {

    private static StatsManager instance;

    private final StatsDatabase database;
    private final StatsCache cache;

    private StatsManager(StatsDatabase database, StatsCache cache) {
        this.database = database;
        this.cache = cache;
    }

    /**
     * Initialises StatsManager, opens the DB, async-loads boot data, registers listener and bStats charts.
     */
    public static void init(File dataFolder, BStatsMetrics metrics) {
        if (instance != null) return;
        try {
            StatsDatabase db = new StatsDatabase(dataFolder);
            StatsCache c = new StatsCache();
            instance = new StatsManager(db, c);

            // Boot load runs on writeExecutor (same thread as inserts) to avoid concurrent Connection use.
            // Listener registers after cache init so no boot-window records are overwritten.
            // The callback hops to the global region thread via SchedulerUtils (Bukkit.getScheduler() throws on Folia).
            StatsManager manager = instance;

            // Queued on the writer executor ahead of the boot load, so the cache never counts
            // records that are about to be pruned. No-op when retention is 0 (keep forever).
            db.pruneAsync(Config.getOperationsRetentionDays());

            db.loadBootDataAsync(
                    bd -> SchedulerUtils.runGlobal(plugin, () -> {
                        try {
                            // Skip if shut down / replaced (e.g. reload) while the boot load was in flight.
                            if (instance != manager) return;
                            c.initialize(bd);
                            Bukkit.getPluginManager().registerEvents(new StatsListener(manager), plugin);
                        } catch (Exception ex) {
                            Logger.getLogger("Disenchantment").log(Level.SEVERE,
                                    "StatsManager failed to apply boot data; stats disabled until reload", ex);
                        }
                    }),
                    e -> Logger.getLogger("Disenchantment").warning("StatsManager boot load failed: " + e.getMessage())
            );

            if (metrics != null) registerCharts(metrics);

        } catch (SQLException e) {
            Logger.getLogger("Disenchantment").warning("StatsManager init failed, stats disabled: " + e.getMessage());
        }
    }

    /**
     * Flushes pending DB writes and clears the singleton instance.
     */
    public static void shutdown() {
        if (instance == null) return;
        instance.database.close();
        instance = null;
    }

    /**
     * Returns the active instance, or null when logging.operations is disabled.
     */
    public static @Nullable StatsManager getInstance() {
        return instance;
    }

    /**
     * Returns the in-memory stats cache.
     */
    public StatsCache getCache() {
        return cache;
    }

    /**
     * Records one operation: sync cache update + async DB write.
     */
    public void record(OperationType type, Player player, ItemStack resultBook,
                       String itemMaterial, int xpCost, double economyCost) {
        record(type, player, StatsListener.extractEnchantmentKeys(resultBook), itemMaterial, xpCost, economyCost);
    }

    /**
     * Records one operation whose enchantment keys were determined by the caller (e.g. an
     * item-to-item transfer, where the result is not a book).
     */
    public void record(OperationType type, Player player, List<String> enchKeys,
                       String itemMaterial, int xpCost, double economyCost) {
        Instant now = Instant.now();

        cache.record(type, enchKeys, player.getUniqueId(), player.getName(),
                xpCost, economyCost, now);

        database.insertAsync(new OperationRecord(
                now.getEpochSecond(),
                player.getUniqueId(),
                player.getName(),
                player.getWorld().getName(),
                type,
                itemMaterial,
                String.join(",", enchKeys),
                xpCost,
                economyCost
        ));
    }

    /**
     * Schedules deletion of operation records older than {@code days} days on the DB writer thread.
     * No-op when {@code days <= 0}. Cached totals are not adjusted; they reflect the DB again on the next boot.
     *
     * @param days maximum record age in days
     */
    public void pruneAsync(int days) {
        database.pruneAsync(days);
    }

    private static void registerCharts(BStatsMetrics metrics) {
        metrics.addCustomChart(new BStatsMetrics.DrilldownPie("top_enchantments", () -> {
            StatsManager m = instance;
            if (m == null) return null;
            Map<String, Long> counts = m.cache.getEnchantmentCounts();
            if (counts.isEmpty()) return null;
            Map<String, Map<String, Integer>> result = new java.util.LinkedHashMap<>();
            counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> result.put(e.getKey(), Map.of("operations", e.getValue().intValue())));
            return result;
        }));

        metrics.addCustomChart(new BStatsMetrics.SimplePie("operation_ratio", () -> {
            StatsManager m = instance;
            if (m == null) return null;
            long d = m.cache.getTotalDisenchants();
            long s = m.cache.getTotalShatters();
            if (d == 0 && s == 0) return null;
            return d >= s ? "Disenchant" : "Shatter";
        }));

        metrics.addCustomChart(new BStatsMetrics.SimplePie("economy_enabled", () -> {
            boolean enabled = com.jankominek.disenchantment.config.Config.Disenchantment.Economy.isEnabled()
                    || com.jankominek.disenchantment.config.Config.Shatterment.Economy.isEnabled();
            return enabled ? "Yes" : "No";
        }));
    }
}
