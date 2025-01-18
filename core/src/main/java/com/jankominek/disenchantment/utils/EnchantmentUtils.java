package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.Disenchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class EnchantmentUtils {
    public static void addEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
        item.addUnsafeEnchantment(enchantment, level);
    }

    public static void addStoredEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storage)
            storage.addStoredEnchant(enchantment, level, true);
        else {
            assert meta != null;
            meta.addEnchant(enchantment, level, true);
        }

        item.setItemMeta(meta);
    }

    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        item.removeEnchantment(enchantment);
    }

    public static void removeStoredEnchantment(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storage)
            storage.removeStoredEnchant(enchantment);
        else {
            assert meta != null;
            meta.removeEnchant(enchantment);
        }

        item.setItemMeta(meta);
    }

    public static boolean canItemBeEnchanted(Material material) {
        return EnchantmentUtils.canItemBeEnchanted(new ItemStack(material));
    }

    public static boolean canItemBeEnchanted(ItemStack item) {
        return Disenchantment.nms.canItemBeEnchanted(item);
    }

    public static HashMap<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        HashMap<Enchantment, Integer> enchantments;

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = new HashMap<>(meta.getStoredEnchants());
        } else {
            enchantments = new HashMap<>(item.getEnchantments());
        }

        return enchantments;
    }

    public static List<Enchantment> getRegisteredEnchantments() {
        return Disenchantment.nms.getRegisteredEnchantments();
    }
}
