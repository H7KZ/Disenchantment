package com.jankominek.disenchantment.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsDatabaseTest {

    private StatsDatabase db;

    @BeforeEach
    void setUp() throws SQLException {
        db = new StatsDatabase("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void insertAndLoad_disenchant_recordedInBootData() throws Exception {
        UUID uuid = UUID.randomUUID();
        OperationRecord record = new OperationRecord(
                1000L, uuid, "Steve", "world",
                OperationType.DISENCHANT, "DIAMOND_SWORD",
                "minecraft:sharpness", 3, 5.0
        );

        db.insertSync(record);

        BootData data = db.loadBootData();
        assertEquals(1, data.disenchants());
        assertEquals(0, data.shatters());
        assertEquals(3, data.xpSpent());
        assertEquals(5.0, data.moneySpent());
    }

    @Test
    void insertAndLoad_shatter_recordedInBootData() throws Exception {
        UUID uuid = UUID.randomUUID();
        OperationRecord record = new OperationRecord(
                2000L, uuid, "Alex", "nether",
                OperationType.SHATTER, "ENCHANTED_BOOK",
                "minecraft:fortune", 2, 0.0
        );

        db.insertSync(record);

        BootData data = db.loadBootData();
        assertEquals(0, data.disenchants());
        assertEquals(1, data.shatters());
    }

    @Test
    void loadBootData_perPlayerStats_aggregatedCorrectly() throws Exception {
        UUID uuid = UUID.randomUUID();
        db.insertSync(new OperationRecord(1000L, uuid, "Steve", "world",
                OperationType.DISENCHANT, "DIAMOND_SWORD", "minecraft:sharpness", 3, 5.0));
        db.insertSync(new OperationRecord(2000L, uuid, "Steve", "world",
                OperationType.SHATTER, "ENCHANTED_BOOK", "minecraft:fortune", 1, 0.0));

        BootData data = db.loadBootData();
        BootData.PlayerBootStats ps = data.playerStats().get(uuid);
        assertNotNull(ps);
        assertEquals(1, ps.disenchants());
        assertEquals(1, ps.shatters());
        assertEquals(4, ps.xpSpent());
        assertEquals(5.0, ps.moneySpent());
        assertEquals(2000L, ps.lastOperationEpoch());
    }

    @Test
    void loadBootData_enchantmentCounts_aggregatedCorrectly() throws Exception {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        db.insertSync(new OperationRecord(1000L, u1, "A", "w", OperationType.DISENCHANT,
                "SWORD", "minecraft:sharpness,minecraft:looting", 1, 0.0));
        db.insertSync(new OperationRecord(2000L, u2, "B", "w", OperationType.DISENCHANT,
                "SWORD", "minecraft:sharpness", 1, 0.0));

        BootData data = db.loadBootData();
        assertEquals(2L, data.enchantmentCounts().get("minecraft:sharpness"));
        assertEquals(1L, data.enchantmentCounts().get("minecraft:looting"));
    }

    @Test
    void pruneOlderThan_deletesOnlyOldRecords(@TempDir File dir) throws Exception {
        StatsDatabase file = new StatsDatabase(dir);
        try {
            long now = Instant.now().getEpochSecond();
            UUID uuid = UUID.randomUUID();
            file.insertSync(new OperationRecord(now - 40L * 86_400, uuid, "Steve", "w",
                    OperationType.DISENCHANT, "SWORD", "minecraft:sharpness", 1, 0.0));
            file.insertSync(new OperationRecord(now - 31L * 86_400, uuid, "Steve", "w",
                    OperationType.SHATTER, "BOOK", "minecraft:looting", 1, 0.0));
            file.insertSync(new OperationRecord(now - 5L * 86_400, uuid, "Steve", "w",
                    OperationType.DISENCHANT, "SWORD", "minecraft:mending", 1, 0.0));

            assertEquals(2, file.pruneOlderThan(30));

            BootData data = file.loadBootData();
            assertEquals(1, data.disenchants());
            assertEquals(0, data.shatters());
            assertEquals(1L, data.enchantmentCounts().get("minecraft:mending"));
        } finally {
            file.close();
        }
    }

    @Test
    void pruneOlderThan_nonPositiveDays_isNoOp() throws Exception {
        long now = Instant.now().getEpochSecond();
        db.insertSync(new OperationRecord(now - 400L * 86_400, UUID.randomUUID(), "Steve", "w",
                OperationType.DISENCHANT, "SWORD", "minecraft:sharpness", 1, 0.0));

        assertEquals(0, db.pruneOlderThan(0));
        assertEquals(0, db.pruneOlderThan(-5));

        assertEquals(1, db.loadBootData().disenchants());
    }

    @Test
    void pruneAsync_runsOnWriterAfterQueuedInserts(@TempDir File dir) throws Exception {
        StatsDatabase file = new StatsDatabase(dir);
        long now = Instant.now().getEpochSecond();
        file.insertAsync(new OperationRecord(now - 100L * 86_400, UUID.randomUUID(), "Steve", "w",
                OperationType.DISENCHANT, "SWORD", "minecraft:sharpness", 1, 0.0));
        file.pruneAsync(30);
        file.close(); // drains the writer executor

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + new File(dir, "stats.db").getAbsolutePath());
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM operations")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void init_dropsLegacyEnchantmentKeysIndex(@TempDir File dir) throws Exception {
        String url = "jdbc:sqlite:" + new File(dir, "stats.db").getAbsolutePath();
        new StatsDatabase(url).close();
        try (Connection c = DriverManager.getConnection(url); Statement st = c.createStatement()) {
            st.execute("CREATE INDEX idx_enchantment_keys ON operations(enchantment_keys)");
        }

        new StatsDatabase(url).close();

        try (Connection c = DriverManager.getConnection(url);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_enchantment_keys'")) {
            assertFalse(rs.next(), "legacy index should be dropped");
        }
    }
}
