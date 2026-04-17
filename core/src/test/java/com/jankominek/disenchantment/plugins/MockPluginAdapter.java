package com.jankominek.disenchantment.plugins;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test-only ISupportedPlugin stub. Returns a fixed set of enchantments for any item
 * that is not a blank BOOK (simulating how real adapters ignore blank books). The list
 * is always returned as a mutable copy so EventUtils can call removeIf on it.
 * Also tracks whether {@link #activate()} was called.
 */
public class MockPluginAdapter implements ISupportedPlugin {
    private final String name;
    private final List<IPluginEnchantment> enchantments;
    private boolean activateCalled = false;

    public MockPluginAdapter(String name, IPluginEnchantment... enchantments) {
        this.name = name;
        this.enchantments = new ArrayList<>(Arrays.asList(enchantments));
    }

    @Override
    public String getName() { return name; }

    @Override
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        if (item == null || item.getType() == Material.BOOK) return List.of();
        return new ArrayList<>(enchantments);
    }

    @Override
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item, World world) {
        return getItemEnchantments(item);
    }

    @Override
    public void activate() { activateCalled = true; }

    public boolean wasActivateCalled() { return activateCalled; }
}
