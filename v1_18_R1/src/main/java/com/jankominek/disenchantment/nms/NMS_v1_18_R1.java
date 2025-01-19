package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nbt.NBT_v1_18_R1.setNBTRepairCost;

public class NMS_v1_18_R1 implements NMS {
    @Override
    public boolean canItemBeEnchanted(ItemStack item) {
        Enchantment[] checkers = {
                Enchantment.BINDING_CURSE,
                Enchantment.DURABILITY,
                Enchantment.MENDING
        };

        return Arrays.stream(checkers).anyMatch(e -> e.canEnchantItem(item));
    }

    @Override
    public List<Enchantment> getRegisteredEnchantments() {
        return List.of(Enchantment.values());
    }

    @Override
    public List<Material> getMaterials() {
        return new ArrayList<>(Arrays.asList(Material.values()));
    }

    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>() {
            {
                add(new plugins.AdvancedEnchantments_v1_18_R1());
                add(new plugins.EcoEnchants_v1_18_R1());
                add(new plugins.EnchantsSquared_v1_18_R1());
                add(new plugins.UberEnchant_v1_18_R1());
            }
        };
    }

    @Override
    public int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return anvilInventory.getRepairCost();
    }

    @Override
    public void setItemRepairCost(ItemStack item, int repairCost) {
        setNBTRepairCost(item, 0);
    }

    @Override
    public void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        anvilInventory.setRepairCost(repairCost);
    }
}
