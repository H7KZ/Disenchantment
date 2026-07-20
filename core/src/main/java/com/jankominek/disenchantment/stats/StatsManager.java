package com.jankominek.disenchantment.stats;

import com.jankominek.disenchantment.utils.BStatsMetrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
            db.loadBootDataAsync(
                    bd -> Bukkit.getScheduler().runTask(plugin, () -> {
                        c.initialize(bd);
                        Bukkit.getPluginManager().registerEvents(new StatsListener(instance), plugin);
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
        List<String> enchKeys = StatsListener.extractEnchantmentKeys(resultBook);
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
