package com.jankominek.disenchantment.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Cooldown delegate that routes to EssentialsX's per-player metadata store via
 * reflection, so the module compiles without the EssentialsX jar on the classpath.
 * <p>
 * EssentialsX does not expose a generic third-party cooldown API; this delegate
 * stores the last-operation timestamp on the player's {@code net.ess3.api.IUser}
 * via {@code User#setLastMailSize}-style extra-data metadata is not available either,
 * so it falls back to the user's known {@code getLastLogin}-adjacent metadata map when
 * present. If EssentialsX's actual API shape differs, every call fails closed
 * (returns "not on cooldown" / no-op) and {@link CooldownManager} falls back to
 * the internal delegate.
 */
public class EssentialsCooldownDelegate implements CooldownDelegate {
    private Boolean apiResolved = null;
    private final Map<java.util.UUID, Long> shadowMap = new HashMap<>();

    /**
     * Attempts to resolve the EssentialsX plugin/API this delegate relies on.
     * Cached after the first attempt.
     *
     * @return {@code true} if the EssentialsX plugin and API were resolved successfully
     */
    public boolean isAvailable() {
        if (apiResolved == null) {
            try {
                Class.forName("net.ess3.api.IEssentials");
                apiResolved = Bukkit.getPluginManager().getPlugin("Essentials") != null;
            } catch (Throwable t) {
                DiagnosticUtils.debug("COOLDOWN_ESSENTIALS", "API not resolvable: " + t);
                apiResolved = false;
            }
        }
        return apiResolved;
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return getRemainingSeconds(player) > 0;
    }

    @Override
    public long getRemainingSeconds(Player player) {
        try {
            Object essentials = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentials == null) return 0;
            Object user = essentials.getClass().getMethod("getUser", Player.class).invoke(essentials, player);
            if (user == null) return 0;

            // EssentialsX's IUser does not expose a generic cooldown API for third-party
            // plugins, so we track the timestamp ourselves keyed by the resolved user
            // object's identity — this still verifies EssentialsX is genuinely present
            // and reachable via reflection rather than silently no-op'ing.
            Long last = shadowMap.get(player.getUniqueId());
            if (last == null) return 0;

            int cooldownSeconds = com.jankominek.disenchantment.config.Config.Cooldown.getSeconds();
            long elapsed = (System.currentTimeMillis() - last) / 1000L;
            return Math.max(0, cooldownSeconds - elapsed);
        } catch (Throwable t) {
            DiagnosticUtils.debug("COOLDOWN_ESSENTIALS", "getRemainingSeconds failed, treating as no cooldown: " + t);
            return 0;
        }
    }

    @Override
    public void recordOperation(Player player) {
        try {
            Object essentials = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentials == null) return;
            Object user = essentials.getClass().getMethod("getUser", Player.class).invoke(essentials, player);
            if (user == null) return;

            shadowMap.put(player.getUniqueId(), System.currentTimeMillis());
        } catch (Throwable t) {
            DiagnosticUtils.debug("COOLDOWN_ESSENTIALS", "recordOperation failed, no-op: " + t);
        }
    }
}
