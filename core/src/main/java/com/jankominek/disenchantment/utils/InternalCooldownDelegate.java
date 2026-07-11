package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default cooldown delegate. Tracks the timestamp of each player's last
 * disenchant/shatter operation purely in-memory via a {@link ConcurrentHashMap},
 * with no persistence and no external dependency.
 */
public class InternalCooldownDelegate implements CooldownDelegate {
    private final ConcurrentHashMap<UUID, Long> lastOperationAt = new ConcurrentHashMap<>();

    @Override
    public boolean isOnCooldown(Player player) {
        return getRemainingSeconds(player) > 0;
    }

    @Override
    public long getRemainingSeconds(Player player) {
        Long last = lastOperationAt.get(player.getUniqueId());
        if (last == null) return 0;

        int cooldownSeconds = Config.Cooldown.getSeconds();
        long elapsedSeconds = (System.currentTimeMillis() - last) / 1000L;
        long remaining = cooldownSeconds - elapsedSeconds;

        return Math.max(0, remaining);
    }

    @Override
    public void recordOperation(Player player) {
        lastOperationAt.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Clears all tracked cooldowns. Used in tests.
     */
    public void clear() {
        lastOperationAt.clear();
    }
}
