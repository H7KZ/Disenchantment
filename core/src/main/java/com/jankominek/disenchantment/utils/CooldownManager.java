package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Orchestrates per-player anvil-operation cooldowns.
 * <p>
 * Delegate priority: CMI &gt; EssentialsX &gt; internal in-memory map.
 * When {@code cooldown.seconds} is 0, all checks are skipped with zero overhead.
 * When a configured delegate (CMI/EssentialsX) is enabled but its plugin isn't
 * present or its API isn't resolvable, a warning is logged once and the manager
 * falls back to the internal delegate.
 */
public class CooldownManager {
    private static final InternalCooldownDelegate INTERNAL = new InternalCooldownDelegate();
    private static final CmiCooldownDelegate CMI = new CmiCooldownDelegate();
    private static final EssentialsCooldownDelegate ESSENTIALS = new EssentialsCooldownDelegate();

    private static boolean warnedCmi = false;
    private static boolean warnedEssentials = false;

    private static CooldownDelegate resolveDelegate() {
        if (Config.Cooldown.isUseCmiEnabled()) {
            if (Bukkit.getPluginManager().getPlugin("CMI") != null && CMI.isAvailable()) return CMI;
            if (!warnedCmi) {
                warnedCmi = true;
                DiagnosticUtils.debug("COOLDOWN", "use-cmi is enabled but CMI is not present/resolvable — falling back to internal cooldown map");
            }
            return INTERNAL;
        }

        if (Config.Cooldown.isUseEssentialsEnabled()) {
            if (Bukkit.getPluginManager().getPlugin("Essentials") != null && ESSENTIALS.isAvailable()) return ESSENTIALS;
            if (!warnedEssentials) {
                warnedEssentials = true;
                DiagnosticUtils.debug("COOLDOWN", "use-essentials is enabled but EssentialsX is not present/resolvable — falling back to internal cooldown map");
            }
            return INTERNAL;
        }

        return INTERNAL;
    }

    /**
     * Returns whether the player is currently prevented from performing another
     * anvil operation. Always {@code false} when cooldowns are disabled (seconds &lt;= 0)
     * or the player holds the bypass permission.
     */
    public static boolean isOnCooldown(Player player) {
        if (Config.Cooldown.getSeconds() <= 0) return false;
        if (player.hasPermission("disenchantment.cooldown.bypass")) return false;

        return resolveDelegate().isOnCooldown(player);
    }

    /**
     * Returns the number of whole seconds remaining before the player may perform
     * another anvil operation. 0 if not on cooldown.
     */
    public static long getRemainingSeconds(Player player) {
        if (Config.Cooldown.getSeconds() <= 0) return 0;

        return resolveDelegate().getRemainingSeconds(player);
    }

    /**
     * Records that the player just completed an anvil operation, starting a new cooldown.
     * No-op when cooldowns are disabled.
     */
    public static void recordOperation(Player player) {
        if (Config.Cooldown.getSeconds() <= 0) return;

        resolveDelegate().recordOperation(player);
    }

    /**
     * Resets cached delegate-availability warnings and clears the internal cooldown map.
     * Should be called during plugin shutdown/reload and is useful in tests.
     */
    public static void reset() {
        warnedCmi = false;
        warnedEssentials = false;
        INTERNAL.clear();
    }
}
