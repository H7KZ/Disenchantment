package com.jankominek.disenchantment.stats;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class StatsDatabase {

    private final Connection connection;
    // ponytail: single-threaded executor serialises writes — avoids SQLite locking
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Disenchantment-Stats-Writer");
        t.setDaemon(true);
        return t;
    });

    /**
     * Production constructor — opens plugins/Disenchantment/stats.db.
     */
    public StatsDatabase(java.io.File dataFolder) throws SQLException {
        this("jdbc:sqlite:" + new java.io.File(dataFolder, "stats.db").getAbsolutePath());
    }

    /**
     * Package-private for tests — accepts any JDBC URL (e.g. "jdbc:sqlite::memory:").
     */
    StatsDatabase(String jdbcUrl) throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl);
        createSchema();
    }

    private void createSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS operations (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        timestamp       INTEGER NOT NULL,
                        player_uuid     TEXT    NOT NULL,
                        player_name     TEXT    NOT NULL,
                        world           TEXT    NOT NULL,
                        operation_type  TEXT    NOT NULL,
                        item_material   TEXT    NOT NULL,
                        enchantment_keys TEXT   NOT NULL,
                        xp_cost         INTEGER NOT NULL,
                        economy_cost    REAL    NOT NULL
                    )""");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_uuid ON operations(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_operation_type ON operations(operation_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enchantment_keys ON operations(enchantment_keys)");
        }
    }

    public void insertAsync(OperationRecord r) {
        writeExecutor.execute(() -> {
            try {
                insertSync(r);
            } catch (SQLException e) {
                Logger.getLogger("Disenchantment").warning("StatsDatabase write failed: " + e.getMessage());
            }
        });
    }

    /**
     * Synchronous insert — used by tests and the async executor.
     */
    void insertSync(OperationRecord r) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO operations (timestamp,player_uuid,player_name,world,operation_type,item_material,enchantment_keys,xp_cost,economy_cost) VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, r.timestamp());
            ps.setString(2, r.playerUuid().toString());
            ps.setString(3, r.playerName());
            ps.setString(4, r.world());
            ps.setString(5, r.operationType().name());
            ps.setString(6, r.itemMaterial());
            ps.setString(7, r.enchantmentKeys());
            ps.setInt(8, r.xpCost());
            ps.setDouble(9, r.economyCost());
            ps.executeUpdate();
        }
    }

    /**
     * Loads aggregate stats for populating the in-memory cache at boot. Runs on async thread.
     */
    public BootData loadBootData() throws SQLException {
        long disenchants = 0, shatters = 0, xpSpent = 0;
        double moneySpent = 0.0;
        Map<String, Long> enchCounts = new HashMap<>();
        Map<UUID, BootData.PlayerBootStats> playerStats = new HashMap<>();

        // Server-wide totals: COUNT + SUM per operation_type
        try (ResultSet rs = connection.createStatement().executeQuery(
                "SELECT operation_type, COUNT(*), SUM(xp_cost), SUM(economy_cost) FROM operations GROUP BY operation_type")) {
            while (rs.next()) {
                String type = rs.getString(1);
                long count = rs.getLong(2);
                long xp = rs.getLong(3);
                double money = rs.getDouble(4);
                if ("DISENCHANT".equals(type)) disenchants = count;
                else if ("SHATTER".equals(type)) shatters = count;
                xpSpent += xp;
                moneySpent += money;
            }
        }

        // Per-player stats
        try (ResultSet rs = connection.createStatement().executeQuery(
                """
                        SELECT player_uuid, player_name,
                          SUM(CASE WHEN operation_type='DISENCHANT' THEN 1 ELSE 0 END),
                          SUM(CASE WHEN operation_type='SHATTER' THEN 1 ELSE 0 END),
                          SUM(xp_cost), SUM(economy_cost), MAX(timestamp)
                        FROM operations GROUP BY player_uuid""")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString(1));
                playerStats.put(uuid, new BootData.PlayerBootStats(
                        rs.getString(2),
                        rs.getLong(3), rs.getLong(4),
                        rs.getLong(5), rs.getDouble(6),
                        rs.getLong(7)));
            }
        }

        // Enchantment counts — aggregate comma-separated keys in Java
        try (ResultSet rs = connection.createStatement().executeQuery(
                "SELECT enchantment_keys FROM operations")) {
            while (rs.next()) {
                String keys = rs.getString(1);
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys.split(",")) {
                        String k = key.trim();
                        if (!k.isEmpty()) enchCounts.merge(k, 1L, Long::sum);
                    }
                }
            }
        }

        return new BootData(disenchants, shatters, xpSpent, moneySpent, enchCounts, playerStats);
    }

    /**
     * Submits the boot load onto writeExecutor so it shares the connection thread with inserts.
     */
    public void loadBootDataAsync(Consumer<BootData> onLoaded, Consumer<SQLException> onError) {
        writeExecutor.submit(() -> {
            try {
                onLoaded.accept(loadBootData());
            } catch (SQLException e) {
                onError.accept(e);
            }
        });
    }

    public void close() {
        writeExecutor.shutdown();
        try {
            writeExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
