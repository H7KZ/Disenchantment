package com.jankominek.disenchantment.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void record_concurrentThreads_noLostUpdates() throws Exception {
        int threads = 8, perThread = 2000;
        UUID uuid = UUID.randomUUID();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            OperationType type = t % 2 == 0 ? OperationType.DISENCHANT : OperationType.SHATTER;
            futures.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < perThread; i++) {
                    cache.record(type, List.of("minecraft:mending"), uuid, "Steve", 2, 0.5, Instant.now());
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        long half = (long) (threads / 2) * perThread;
        assertEquals(half, cache.getTotalDisenchants());
        assertEquals(half, cache.getTotalShatters());
        assertEquals(2L * threads * perThread, cache.getTotalXpSpent());
        assertEquals(0.5 * threads * perThread, cache.getTotalMoneySpent(), 1e-6);
        assertEquals((long) threads * perThread, cache.getEnchantmentCount("minecraft:mending"));
        StatsCache.PlayerStats ps = cache.getPlayerStats(uuid);
        assertEquals(half, ps.disenchants());
        assertEquals(half, ps.shatters());
        assertEquals(2L * threads * perThread, ps.xpSpent());
    }

    @Test
    void initialize_afterRecord_addsInsteadOfOverwriting() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.DISENCHANT, List.of("minecraft:mending"), uuid, "Steve", 1, 0.0, Instant.ofEpochSecond(5000));
        BootData bd = new BootData(2L, 0L, 4L, 0.0, Map.of("minecraft:mending", 2L),
                Map.of(uuid, new BootData.PlayerBootStats("Steve", 2L, 0L, 4L, 0.0, 1000L)));

        cache.initialize(bd);

        assertEquals(3, cache.getTotalDisenchants());
        assertEquals(5, cache.getTotalXpSpent());
        assertEquals(3L, cache.getEnchantmentCount("minecraft:mending"));
        assertEquals(3, cache.getPlayerStats(uuid).disenchants());
        assertEquals(Instant.ofEpochSecond(5000), cache.getPlayerStats(uuid).lastOperation());
    }
    @Test
    void record_grindstoneTypes_areNotCountedAsDisenchantOrShatter() {
        UUID uuid = UUID.randomUUID();
        cache.record(OperationType.GRIND_EXTRACT, List.of("minecraft:sharpness"), uuid, "Steve", 4, 0.0, Instant.now());
        cache.record(OperationType.GRIND_TRANSFER, List.of("minecraft:mending"), uuid, "Steve", 6, 2.0, Instant.now());

        assertEquals(0, cache.getTotalDisenchants());
        assertEquals(0, cache.getTotalShatters(), "Grindstone operations must not inflate the shatter total");
        assertEquals(1L, cache.getEnchantmentCounts().get("minecraft:sharpness"));
        assertEquals(1L, cache.getEnchantmentCounts().get("minecraft:mending"));
        assertEquals(10, cache.getTotalXpSpent());
    }
}
