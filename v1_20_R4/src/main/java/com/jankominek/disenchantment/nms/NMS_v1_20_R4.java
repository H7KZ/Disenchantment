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

import static nbt.NBT_v1_20_R4.setNBTRepairCost;

public class NMS_v1_20_R4 implements NMS {
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
        return List.of(Enchantment.values());
    }

    @Override
    public List<Material> getMaterials() {
        return new ArrayList<>(Arrays.asList(Material.values()));
    }

    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>();
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
