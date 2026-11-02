package com.jankominek.disenchantment.stats;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsManagerTest extends DisenchantmentTestBase {

    @TempDir
    File dataFolder;

    @AfterEach
    void tearDownStats() {
        StatsManager.shutdown();
    }

    @Test
    void init_bootDataIsAppliedToCacheViaSchedulerAndListenerRegistered() throws Exception {
        StatsDatabase seed = new StatsDatabase(dataFolder);
        seed.insertSync(new OperationRecord(1000L, UUID.randomUUID(), "Steve", "world",
                OperationType.DISENCHANT, "DIAMOND_SWORD", "minecraft:sharpness", 3, 1.5));
        seed.close();

        StatsManager.init(dataFolder, null);
        StatsManager manager = StatsManager.getInstance();
        assertNotNull(manager);

        long deadline = System.currentTimeMillis() + 10_000;
        while (manager.getCache().getTotalDisenchants() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
            server.getScheduler().performOneTick();
        }

        assertEquals(1, manager.getCache().getTotalDisenchants());
        assertEquals(3, manager.getCache().getTotalXpSpent());
        assertTrue(HandlerList.getRegisteredListeners(plugin).stream()
                        .anyMatch(l -> l.getListener() instanceof StatsListener),
                "StatsListener should be registered after boot data applied");
    }
}
