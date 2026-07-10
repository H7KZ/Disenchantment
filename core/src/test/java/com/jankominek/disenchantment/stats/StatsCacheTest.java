package com.jankominek.disenchantment.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatsCacheTest {

    private StatsCache cache;

    @BeforeEach
    void setUp() {
        cache = new StatsCache();
    }

    @Test
    void record_disenchant_incrementsServerAndPlayerCounters() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.DISENCHANT, List.of("minecraft:sharpness"), uuid, "Steve", 3, 5.0, Instant.now());

        assertEquals(1, cache.getTotalDisenchants());
        assertEquals(0, cache.getTotalShatters());
        assertEquals(3, cache.getTotalXpSpent());
        assertEquals(5.0, cache.getTotalMoneySpent());

        StatsCache.PlayerStats ps = cache.getPlayerStats(uuid);
        assertNotNull(ps);
        assertEquals(1, ps.disenchants());
        assertEquals(0, ps.shatters());
    }

    @Test
    void record_shatter_incrementsServerAndPlayerCounters() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.SHATTER, List.of("minecraft:fortune"), uuid, "Alex", 2, 0.0, Instant.now());

        assertEquals(0, cache.getTotalDisenchants());
        assertEquals(1, cache.getTotalShatters());
        assertEquals(2, cache.getTotalXpSpent());
        assertEquals(0.0, cache.getTotalMoneySpent());
    }

    @Test
    void record_multipleOps_samePlayer_accumulates() {
        UUID uuid = UUID.randomUUID();
        Instant t1 = Instant.ofEpochSecond(1000);
        Instant t2 = Instant.ofEpochSecond(2000);
        cache.record(OperationType.DISENCHANT, List.of("minecraft:sharpness"), uuid, "Steve", 3, 0.0, t1);
        cache.record(OperationType.SHATTER, List.of("minecraft:fortune"), uuid, "Steve", 1, 0.0, t2);

        StatsCache.PlayerStats ps = cache.getPlayerStats(uuid);
        assertEquals(1, ps.disenchants());
        assertEquals(1, ps.shatters());
        assertEquals(4, ps.xpSpent());
        assertEquals(t2, ps.lastOperation());
    }

    @Test
    void record_enchantmentCounts_aggregated() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.DISENCHANT, List.of("minecraft:sharpness", "minecraft:looting"), uuid, "A", 1, 0.0, Instant.now());
        cache.record(OperationType.DISENCHANT, List.of("minecraft:sharpness"), uuid, "A", 1, 0.0, Instant.now());

        assertEquals(2L, cache.getEnchantmentCount("minecraft:sharpness"));
        assertEquals(1L, cache.getEnchantmentCount("minecraft:looting"));
    }

    @Test
    void getTopEnchantment_returnsHighestCount() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.DISENCHANT, List.of("minecraft:sharpness", "minecraft:sharpness"), uuid, "A", 1, 0.0, Instant.now());
        cache.record(OperationType.DISENCHANT, List.of("minecraft:fortune"), uuid, "A", 1, 0.0, Instant.now());

        assertEquals("minecraft:sharpness", cache.getTopEnchantment());
    }

    @Test
    void initialize_fromBootData_setsAllFields() {
        UUID uuid = UUID.randomUUID();
        Map<String, Long> encCounts = Map.of("minecraft:sharpness", 5L);
        Map<UUID, BootData.PlayerBootStats> playerStats = Map.of(
                uuid, new BootData.PlayerBootStats("Steve", 3L, 1L, 10L, 2.5, 9999L)
        );
        BootData bd = new BootData(3L, 1L, 10L, 2.5, encCounts, playerStats);

        cache.initialize(bd);

        assertEquals(3, cache.getTotalDisenchants());
        assertEquals(1, cache.getTotalShatters());
        assertEquals(10, cache.getTotalXpSpent());
        assertEquals(2.5, cache.getTotalMoneySpent());
        assertEquals(5L, cache.getEnchantmentCount("minecraft:sharpness"));
        StatsCache.PlayerStats ps = cache.getPlayerStats(uuid);
        assertNotNull(ps);
        assertEquals(3, ps.disenchants());
        assertEquals(1, ps.shatters());
    }
}
