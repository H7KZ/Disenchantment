package com.jankominek.disenchantment;

import com.jankominek.disenchantment.utils.BStatsMetrics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisenchantmentLifecycleTest extends DisenchantmentTestBase {

    private BStatsMetrics currentMetrics() throws Exception {
        Field f = Disenchantment.class.getDeclaredField("metrics");
        f.setAccessible(true);
        return (BStatsMetrics) f.get(plugin);
    }

    private static ScheduledExecutorService schedulerOf(BStatsMetrics metrics) throws Exception {
        Field baseField = BStatsMetrics.class.getDeclaredField("metricsBase");
        baseField.setAccessible(true);
        Object base = baseField.get(metrics);
        Field schedulerField = base.getClass().getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        return (ScheduledExecutorService) schedulerField.get(base);
    }

    @Test
    void reload_shutsDownPreviousBStatsSubmitter() throws Exception {
        BStatsMetrics before = currentMetrics();
        assertNotNull(before);
        assertFalse(schedulerOf(before).isShutdown());

        plugin.reload();

        BStatsMetrics after = currentMetrics();
        assertNotNull(after);
        assertNotSame(before, after);
        assertTrue(schedulerOf(before).isShutdown(), "old bStats submitter must be shut down on reload");
        assertFalse(schedulerOf(after).isShutdown());
    }

    @Test
    void disable_shutsDownBStatsSubmitter() throws Exception {
        BStatsMetrics current = currentMetrics();
        assertNotNull(current);

        plugin.disable();

        assertTrue(schedulerOf(current).isShutdown());
        assertNull(currentMetrics());
    }
}
