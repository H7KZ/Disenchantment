package com.jankominek.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
                break;
            case SHATTERMENT:
                if (!Config.Shatterment.Anvil.Repair.isCostEnabled()) {
                    DiagnosticUtils.debug(category, "AnvilCost: XP cost disabled by config → 0");
                    return 0;
                }

                totalCost = Config.Shatterment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();
                overrides = Config.Shatterment.Anvil.Repair.getEnchantmentCosts();
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
            if (overrides.containsKey(key)) {
                totalCost += overrides.get(key);
                // do NOT advance multiplier for overridden enchantments
            } else {
                totalCost += enchantment.getLevel() * multiplier.get();
                multiplier.set(multiplier.get() + baseMultiplier);
            }
        }

        int result = (int) Math.round(totalCost);
        DiagnosticUtils.debug(category, "AnvilCost: calculated=" + result + " XP levels");
        return result;
    }
}
