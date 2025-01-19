package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import plugins.ExcellentEnchants_v1_21_R1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NMS_v1_21_R1 implements NMS {
    @Override
    public boolean canItemBeEnchanted(ItemStack item) {
        Enchantment[] checkers = {
                Enchantment.BINDING_CURSE,
                Enchantment.UNBREAKING,
                Enchantment.MENDING
        };

        return Arrays.stream(checkers).anyMatch(e -> e.canEnchantItem(item));
    }

    @Override
    public List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>(Registry.ENCHANTMENT.stream().toList());
    }

    @Override
    public List<Material> getMaterials() {
        return new ArrayList<>(Registry.MATERIAL.stream().toList());
    }

    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>() {
            {
                add(new plugins.AdvancedEnchantments_v1_21_R1());
                add(new plugins.EcoEnchants_v1_21_R1());
                add(new plugins.EnchantsSquared_v1_21_R1());
                add(new ExcellentEnchants_v1_21_R1());
                add(new plugins.UberEnchant_v1_21_R1());
            }
        };
    }

    @Override
    public int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        AnvilView anvilView = (AnvilView) inventoryView;

        return anvilView.getRepairCost();
    }

    @Override
    public void setItemRepairCost(ItemStack item, int repairCost) {
        if (item.getItemMeta() instanceof Repairable meta) {
            meta.setRepairCost(0);
            item.setItemMeta(meta);
        }
    }

    @Override
    public void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        AnvilView anvilView = (AnvilView) inventoryView;

        anvilView.setRepairCost(repairCost);
    }
}
