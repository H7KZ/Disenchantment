package com.jankominek.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.nms;

public class AnvilCostUtils {
    public static int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return nms.getRepairCost(anvilInventory, inventoryView);
    }

    public static void setItemRepairCost(ItemStack item, int repairCost) {
        nms.setItemRepairCost(item, repairCost);
    }

    public static void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        nms.setAnvilRepairCost(anvilInventory, inventoryView, repairCost);
    }

    public static int countAnvilCost(Map<Enchantment, Integer> enchantments, AnvilEventType anvilEventType) {
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

        Map<Enchantment, Integer> sortedEnchantments = MapUtils.sortByValue(enchantments, true);

        for (Map.Entry<Enchantment, Integer> entry : sortedEnchantments.entrySet()) {
            int level = entry.getValue();

            enchantmentCost += level * multiplier.get();
            multiplier.set(multiplier.get() + baseMultiplier);
        }

        return (int) Math.round(enchantmentCost);
    }
}
