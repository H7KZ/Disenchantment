package org.geysermc.floodgate.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test double with the shape of Floodgate's real {@code FloodgateApi} (static {@code getInstance()},
 * {@code isFloodgatePlayer(UUID)}), so the reflection in {@code BedrockUtils} can be exercised without the real
 * dependency. Lives under the real package name on the test classpath only.
 */
public class FloodgateApi {
    private static final FloodgateApi INSTANCE = new FloodgateApi();

    /**
     * UUIDs this fake reports as Floodgate players.
     */
    public static final Set<UUID> BEDROCK = Collections.synchronizedSet(new HashSet<>());

    /**
     * When {@code true}, {@code getInstance()} throws, to prove detection never propagates API failures.
     */
    public static volatile boolean broken;

    public static FloodgateApi getInstance() {
        if (broken) throw new IllegalStateException("Floodgate is not ready");
        return INSTANCE;
    }

    public boolean isFloodgatePlayer(UUID uuid) {
        return BEDROCK.contains(uuid);
    }

    /**
     * Restores the defaults between tests.
     */
    public static void reset() {
        BEDROCK.clear();
        broken = false;
    }
}
