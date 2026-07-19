package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.entity.Player;

/**
 * Resolves the XP cost multiplier for a player's anvil operation.
 * <p>
 * Resolution order (first match wins):
 * <ol>
 *     <li>LuckPerms {@code disenchantment_multiplier} meta value (if installed and enabled)</li>
 *     <li>Highest matching {@code disenchantment.discount.N} permission node (N = 5, 10, ..., 95)</li>
 *     <li>Default 1.0 (no discount)</li>
 * </ol>
 */
public class CostMultiplierUtils {
    /**
     * Resolves the cost multiplier for the given player.
     *
     * @param player the player performing the anvil operation
     * @return the multiplier to apply to the calculated XP cost
     */
    public static double getMultiplier(Player player) {
        if (player == null) return 1.0;

        if (Config.CostMultiplier.isLuckPermsEnabled() && LuckPermsUtils.isAvailable()) {
            Double meta = LuckPermsUtils.getMultiplier(player);
            if (meta != null) return Math.max(0.0, meta);
        }

        int bestDiscount = 0;
        for (int discount = 5; discount <= 95; discount += 5) {
            if (discount > bestDiscount && player.hasPermission("disenchantment.discount." + discount)) {
                bestDiscount = discount;
            }
        }

        if (bestDiscount > 0) {
            double multiplier = 1.0 - (bestDiscount / 100.0);
            DiagnosticUtils.debug("COST_MULTIPLIER", "Permission-node discount for " + player.getName() + " → " + bestDiscount + "% (multiplier=" + multiplier + ")");
            return multiplier;
        }

        return 1.0;
    }
}
