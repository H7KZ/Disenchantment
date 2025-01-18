package com.jankominek.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.Map;

public class AnvilCostUtils {
    public static int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return Disenchantment.nms.getRepairCost(anvilInventory, inventoryView);
    }

    public static void setItemRepairCost(ItemStack item, int repairCost) {
        Disenchantment.nms.setItemRepairCost(item, repairCost);
    }

    public static void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        Disenchantment.nms.setAnvilRepairCost(anvilInventory, inventoryView, repairCost);
    }

    public static Integer countAnvilCost(Map<Enchantment, Integer> enchantments, AnvilEventType anvilEventType) {
        double enchantmentCost = 0;
        double baseMultiplier = 0;

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

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toList()) {
            enchantmentCost += entry.getValue() * multiplier.get();
            multiplier.set(multiplier.get() + baseMultiplier);
        }

        return (int) Math.round(enchantmentCost);
    }
}
