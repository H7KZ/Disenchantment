package com.jankominek.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.nms;

/**
 * Utility class for computing and managing anvil repair costs.
 * Delegates NMS-specific operations to the version-appropriate NMS implementation.
 */
public class AnvilCostUtils {
    /**
     * Retrieves the current repair cost from the anvil inventory via NMS.
     *
     * @param anvilInventory the anvil inventory
     * @param inventoryView  the player's inventory view
     * @return the current repair cost in experience levels
     */
    public static int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return nms.getRepairCost(anvilInventory, inventoryView);
    }

    /**
     * Sets the repair cost on an individual item stack via NMS.
     *
     * @param item       the item to modify
     * @param repairCost the repair cost to set
     */
    public static void setItemRepairCost(ItemStack item, int repairCost) {
        nms.setItemRepairCost(item, repairCost);
    }

    /**
     * Sets the repair cost displayed in the anvil GUI via NMS.
     *
     * @param anvilInventory the anvil inventory
     * @param inventoryView  the player's inventory view
     * @param repairCost     the repair cost to set
     */
    public static void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        nms.setAnvilRepairCost(anvilInventory, inventoryView, repairCost);
    }

    /**
     * Calculates the cost for a single enchantment, using a fixed override if present,
     * otherwise falling back to the formula: base + level * multiplier.
     *
     * @param key        the namespaced key string of the enchantment (e.g. "minecraft:mending")
     * @param level      the enchantment level
     * @param base       the base cost from config
     * @param multiplier the per-enchantment multiplier
     * @param overrides  map of enchantment key → fixed cost overrides
     * @return the cost for this enchantment
     */
    public static int costForEnchantment(String key, int level, double base, double multiplier, Map<String, Integer> overrides) {
        if (overrides.containsKey(key)) return overrides.get(key);
        // Also try the short key without namespace (e.g. "mending" for "minecraft:mending")
        int colon = key.indexOf(':');
        if (colon >= 0) {
            String shortKey = key.substring(colon + 1);
            if (overrides.containsKey(shortKey)) return overrides.get(shortKey);
        }
        return (int) Math.round(base + level * multiplier);
    }

    /**
     * Calculates the total anvil cost for a list of enchantments based on the event type
     * (disenchantment or shatterment). Uses configured base cost and multiplier, applying
     * an incrementing multiplier for each enchantment sorted by level descending.
     * Per-enchantment overrides bypass the formula entirely and do not advance the multiplier.
     *
     * @param enchantments   the enchantments involved in the operation
     * @param anvilEventType the type of anvil event (DISENCHANTMENT or SHATTERMENT)
     * @return the calculated cost in experience levels, or 0 if cost is disabled
     */
    public static int countAnvilCost(List<IPluginEnchantment> enchantments, AnvilEventType anvilEventType) {
        double totalCost;
        double baseMultiplier;
        Map<String, Integer> overrides;
        int maxCost;
        String category = anvilEventType == AnvilEventType.DISENCHANTMENT ? "DISENCHANT" : "SHATTER";

        switch (anvilEventType) {
            case DISENCHANTMENT:
                if (!Config.Disenchantment.Anvil.Repair.isCostEnabled()) {
                    DiagnosticUtils.debug(category, "AnvilCost: XP cost disabled by config → 0");
                    return 0;
                }

                totalCost = Config.Disenchantment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Disenchantment.Anvil.Repair.getCostMultiplier();
                overrides = Config.Disenchantment.Anvil.Repair.getEnchantmentCosts();
                maxCost = Config.Disenchantment.Anvil.Repair.getMaxCost();
                break;
            case SHATTERMENT:
                if (!Config.Shatterment.Anvil.Repair.isCostEnabled()) {
                    DiagnosticUtils.debug(category, "AnvilCost: XP cost disabled by config → 0");
                    return 0;
                }

                totalCost = Config.Shatterment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();
                overrides = Config.Shatterment.Anvil.Repair.getEnchantmentCosts();
                maxCost = Config.Shatterment.Anvil.Repair.getMaxCost();
                break;
            default:
                return 0;
        }

        DiagnosticUtils.debug(category, "AnvilCost: base=" + totalCost + ", multiplier=" + baseMultiplier + ", enchantments=" + enchantments.size());

        AtomicDouble multiplier = new AtomicDouble(baseMultiplier);

        // sort by level descending
        IPluginEnchantment[] sortedEnchantments = enchantments.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getLevel(), e1.getLevel()))
                .toArray(IPluginEnchantment[]::new);

        for (IPluginEnchantment enchantment : sortedEnchantments) {
            String key = enchantment.getKey() != null ? enchantment.getKey() : "";
            String shortKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
            if (overrides.containsKey(key) || overrides.containsKey(shortKey)) {
                // fixed override cost — does not interact with the level*multiplier formula
                totalCost += overrides.containsKey(key) ? overrides.get(key) : overrides.get(shortKey);
                // do NOT advance multiplier for overridden enchantments
            } else {
                // formula path — delegate to costForEnchantment with base=0 (base already seeded into totalCost)
                totalCost += costForEnchantment(key, enchantment.getLevel(), 0, multiplier.get(), Map.of());
                multiplier.set(multiplier.get() + baseMultiplier);
            }
        }

        int result = (int) Math.round(totalCost);

        if (maxCost > 0 && result > maxCost) {
            DiagnosticUtils.debug(category, "AnvilCost: capped " + result + " → " + maxCost + " (max-cost)");
            result = maxCost;
        }

        DiagnosticUtils.debug(category, "AnvilCost: calculated=" + result + " XP levels");
        return result;
    }

    /**
     * Calculates the total economy cost for a set of enchantments. When one or more
     * enchantments has a per-enchantment economy override, the total is the sum of those
     * overrides plus the flat {@code economy.cost} once if any enchantment lacks an override.
     * When no enchantment has an override, the total is simply the flat cost (legacy behavior).
     *
     * @param enchantments      the enchantments involved in the operation
     * @param flatCost          the configured flat economy cost (economy.cost)
     * @param economyOverrides  map of enchantment key → per-enchantment economy cost override
     * @return the total economy cost to charge
     */
    public static double economyCostForEnchantments(List<IPluginEnchantment> enchantments, double flatCost, Map<String, Double> economyOverrides) {
        if (economyOverrides == null || economyOverrides.isEmpty()) return flatCost;

        double total = 0;
        boolean hasUnoverridden = false;

        for (IPluginEnchantment enchantment : enchantments) {
            String key = enchantment.getKey() != null ? enchantment.getKey() : "";
            String shortKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;

            if (economyOverrides.containsKey(key)) {
                total += economyOverrides.get(key);
            } else if (economyOverrides.containsKey(shortKey)) {
                total += economyOverrides.get(shortKey);
            } else {
                hasUnoverridden = true;
            }
        }

        if (hasUnoverridden) total += flatCost;

        return total;
    }
}
