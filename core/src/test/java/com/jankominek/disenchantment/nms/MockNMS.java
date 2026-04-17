package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.guis.HeadBuilder;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test-only NMS stub. All methods use Bukkit API only — no reflection, no NMS internals.
 * Injected via Mockito.mockStatic(NMSMapper.class) before each test so onEnable() succeeds
 * without a real NMS jar on the classpath.
 */
public class MockNMS implements NMS {

    @Override
    public boolean canItemBeEnchanted(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        Material type = item.getType();
        return type != Material.BOOK && type != Material.ENCHANTED_BOOK && type != Material.WRITTEN_BOOK;
    }

    @Override
    public List<Enchantment> getRegisteredEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        Registry.ENCHANTMENT.forEach(list::add);
        return list;
    }

    @Override
    public List<Material> getMaterials() {
        return Arrays.asList(Material.values());
    }

    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return List.of();
    }

    @Override
    public int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return anvilInventory.getRepairCost();
    }

    @Override
    public void setItemRepairCost(ItemStack item, int repairCost) {
        // No NBT in tests — ItemMeta has no repair cost API, intentional no-op
    }

    @Override
    public void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        anvilInventory.setRepairCost(repairCost);
    }

    @Override
    public HeadBuilder setTexture(HeadBuilder headBuilder, String texture) {
        return headBuilder;
    }
}
