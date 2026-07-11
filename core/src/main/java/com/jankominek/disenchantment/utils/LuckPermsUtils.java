package com.jankominek.disenchantment.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Utility class that manages the optional LuckPerms soft dependency.
 * All LuckPerms operations are routed through this class via reflection so that
 * the rest of the codebase never references LuckPerms classes directly and can
 * safely call these methods regardless of whether LuckPerms is installed or
 * whether its API is present on the compile-time classpath.
 */
public class LuckPermsUtils {
    private static Boolean available = null;

    /**
     * Returns whether the LuckPerms plugin is currently present on the server.
     * The result is cached after the first check.
     *
     * @return {@code true} if LuckPerms is installed
     */
    public static boolean isAvailable() {
        if (available == null) {
            available = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
            DiagnosticUtils.debug("LUCKPERMS", "Availability check: " + available);
        }
        return available;
    }

    /**
     * Reads the {@code disenchantment_multiplier} meta value from the player's
     * effective LuckPerms permission context via reflection.
     *
     * @param player the player to look up
     * @return the parsed multiplier, or {@code null} if LuckPerms is unavailable,
     * the user has no cached data, the meta key is unset, or the value is not a valid number
     */
    public static Double getMultiplier(Player player) {
        if (player == null || !isAvailable()) return null;

        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);

            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());
            if (user == null) {
                DiagnosticUtils.debug("LUCKPERMS", "getMultiplier(" + player.getName() + ") → no cached user");
                return null;
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object value = metaData.getClass().getMethod("getMetaValue", String.class).invoke(metaData, "disenchantment_multiplier");

            if (value == null) return null;

            double multiplier = Double.parseDouble(value.toString());
            DiagnosticUtils.debug("LUCKPERMS", "getMultiplier(" + player.getName() + ") → " + multiplier);
            return multiplier;
        } catch (NumberFormatException e) {
            DiagnosticUtils.debug("LUCKPERMS", "getMultiplier(" + player.getName() + ") → non-numeric meta value");
            return null;
        } catch (Throwable t) {
            // Covers ClassNotFoundException, NoSuchMethodError, NoClassDefFoundError and any
            // other reflection failure — the LuckPerms API shape differs or isn't resolvable.
            DiagnosticUtils.debug("LUCKPERMS", "getMultiplier(" + player.getName() + ") → reflection failed: " + t);
            return null;
        }
    }

    /**
     * Clears the cached availability flag. Should be called during plugin shutdown/reload.
     */
    public static void reset() {
        available = null;
    }
}
