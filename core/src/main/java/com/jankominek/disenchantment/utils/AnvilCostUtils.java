package com.jankominek.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
     * Calculates the total anvil cost for a list of enchantments based on the event type
     * (disenchantment or shatterment). Uses configured base cost and multiplier, applying
     * an incrementing multiplier for each enchantment sorted by level descending.
     *
     * @param enchantments   the enchantments involved in the operation
     * @param anvilEventType the type of anvil event (DISENCHANTMENT or SHATTERMENT)
     * @return the calculated cost in experience levels, or 0 if cost is disabled
     */
    public static int countAnvilCost(List<IPluginEnchantment> enchantments, AnvilEventType anvilEventType) {
        double enchantmentCost;
        double baseMultiplier;

        switch (anvilEventType) {
            case DISENCHANTMENT:
                if (!Config.Disenchantment.Anvil.Repair.isCostEnabled()) return 0;

                enchantmentCost = Config.Disenchantment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Disenchantment.Anvil.Repair.getCostMultiplier();

                break;
            case SHATTERMENT:
                if (!Config.Shatterment.Anvil.Repair.isCostEnabled()) return 0;

                enchantmentCost = Config.Shatterment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();

                break;
            default:
                return 0;
        }

        AtomicDouble multiplier = new AtomicDouble(baseMultiplier);

        // sort by value
        IPluginEnchantment[] sortedEnchantments = enchantments.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getLevel(), e1.getLevel()))
                .toArray(IPluginEnchantment[]::new);

        for (IPluginEnchantment enchantment : sortedEnchantments) {
            int level = enchantment.getLevel();

            enchantmentCost += level * multiplier.get();
            multiplier.set(multiplier.get() + baseMultiplier);
        }

        return (int) Math.round(enchantmentCost);
    }
}
