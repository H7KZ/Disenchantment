package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Detection against a live (MockBukkit) server. Guards the class-initialisation order: the
 * detected version must come from the real version strings, not a swallowed failure.
 */
class MinecraftVersionDetectionTest extends DisenchantmentTestBase {

    @Test
    void givenRunningServer_whenDetect_thenVersionRecognised() {
        MinecraftVersion detected = MinecraftVersion.detect(server);

        assertNotEquals(MinecraftVersion.INCOMPATIBLE, detected,
                "MockBukkit version strings must resolve: " + server.getVersion() + " / " + server.getBukkitVersion());
    }

    @Test
    void givenRunningServer_whenGetServerVersion_thenMatchesDetection() {
        assertEquals(MinecraftVersion.detect(server), MinecraftVersion.getServerVersion());
    }
}
