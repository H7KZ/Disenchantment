package com.jankominek.disenchantment.plugins;

import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class VanillaPlugin {
    public static ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> EnchantmentUtils.addStoredEnchantment(book, en, l));

        return book;
    }

    public static ItemStack addEnchantmentToBook(ItemStack firstItem, Enchantment enchantment, int level) {
        ItemStack item = firstItem.clone();
        EnchantmentUtils.addStoredEnchantment(item, enchantment, level);
        return item;
    }

    public static ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            EnchantmentUtils.removeStoredEnchantment(item, enchantment);
        }

        return item;
    }

    public static ItemStack removeEnchantment(ItemStack firstItem, Enchantment enchantment) {
        ItemStack item = firstItem.clone();
        EnchantmentUtils.removeStoredEnchantment(item, enchantment);
        return item;
    }
}
