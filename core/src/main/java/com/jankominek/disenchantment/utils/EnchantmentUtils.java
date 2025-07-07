package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.nms;

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

    public static ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();

            EnchantmentUtils.removeStoredEnchantment(item, enchantment);
        }

        return item;
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
        return nms.canItemBeEnchanted(item);
    }

    public static List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        List<IPluginEnchantment> enchantments;

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = meta.getStoredEnchants()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() > 0)
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        } else {
            enchantments = item.getEnchantments()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() > 0)
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        return enchantments;
    }

    public static List<Enchantment> getRegisteredEnchantments() {
        return nms.getRegisteredEnchantments();
    }

    public static IPluginEnchantment remapEnchantment(Enchantment enchantment, int level) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return enchantment.getKey().getKey().toLowerCase();
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack item = book.clone();
                EnchantmentUtils.addStoredEnchantment(item, enchantment, this.getLevel());
                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();
                EnchantmentUtils.removeStoredEnchantment(item, enchantment);
                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();
                EnchantmentUtils.addStoredEnchantment(result, enchantment, this.getLevel());
                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();
                EnchantmentUtils.removeStoredEnchantment(result, enchantment);
                return result;
            }
        };
    }
}
