package com.jankominek.disenchantment.plugins;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface ISupportedPlugin {
    String getName();

    ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments);

    ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments);

    default void activate() {
    }
}
