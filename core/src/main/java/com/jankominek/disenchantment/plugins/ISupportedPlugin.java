package com.jankominek.disenchantment.plugins;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ISupportedPlugin {
    String getName();

    List<IPluginEnchantment> getItemEnchantments(ItemStack item);

    default void activate() {
    }
}
