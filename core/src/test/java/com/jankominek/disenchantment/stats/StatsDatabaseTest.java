package com.jankominek.disenchantment.stats;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
}
