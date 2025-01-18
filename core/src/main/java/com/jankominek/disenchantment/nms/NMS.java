package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface NMS {
    default boolean canItemBeEnchanted(ItemStack item) {
        return false;
    }

    default List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>();
    }

    default List<Material> getMaterials() {
        return new ArrayList<>();
    }

    default List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>();
    }

    default int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return 0;
    }

    default void setItemRepairCost(ItemStack item, int repairCost) {
    }

    default void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
    }
}
