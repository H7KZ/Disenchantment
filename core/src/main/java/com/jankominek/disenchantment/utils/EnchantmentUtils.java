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

/**
 * Utility class for adding, removing, and querying enchantments on items and enchanted books.
 * Handles both regular item enchantments and stored enchantments on books.
 */
public class EnchantmentUtils {
    /**
     * Adds an enchantment directly to an item, bypassing level restrictions.
     *
     * @param item        the item to enchant
     * @param enchantment the enchantment to add
     * @param level       the enchantment level
     */
    public static void addEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
        item.addUnsafeEnchantment(enchantment, level);
    }

    /**
     * Adds a stored enchantment to an item. Uses {@link EnchantmentStorageMeta} for books,
     * or regular enchant meta for other items.
     *
     * @param item        the item to enchant
     * @param enchantment the enchantment to store
     * @param level       the enchantment level
     */
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

    /**
     * Creates a clone of the item with the specified enchantments removed.
     *
     * @param firstItem    the item to clone and modify
     * @param enchantments the enchantments to remove
     * @return a new item stack with the enchantments removed
     */
    public static ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();

            EnchantmentUtils.removeStoredEnchantment(item, enchantment);
        }

        return item;
    }

    /**
     * Removes an enchantment directly from an item.
     *
     * @param item        the item to modify
     * @param enchantment the enchantment to remove
     */
    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        item.removeEnchantment(enchantment);
    }

    /**
     * Removes a stored enchantment from an item. Uses {@link EnchantmentStorageMeta} for books,
     * or regular enchant meta for other items.
     *
     * @param item        the item to modify
     * @param enchantment the enchantment to remove
     */
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

    /**
     * Checks whether items of the given material can be enchanted.
     *
     * @param material the material to check
     * @return true if the material can be enchanted
     */
    public static boolean canItemBeEnchanted(Material material) {
        return EnchantmentUtils.canItemBeEnchanted(new ItemStack(material));
    }

    /**
     * Checks whether the given item stack can be enchanted, via NMS.
     *
     * @param item the item to check
     * @return true if the item can be enchanted
     */
    public static boolean canItemBeEnchanted(ItemStack item) {
        return nms.canItemBeEnchanted(item);
    }

    /**
     * Retrieves all enchantments from an item, supporting both regular and stored enchantments.
     * Filters out null enchantments and those with level 0 or below.
     *
     * @param item the item to inspect
     * @return a mutable list of {@link IPluginEnchantment} instances
     */
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

    /**
     * Returns all registered enchantments from the server via NMS.
     *
     * @return a list of all registered {@link Enchantment} instances
     */
    public static List<Enchantment> getRegisteredEnchantments() {
        return nms.getRegisteredEnchantments();
    }

    /**
     * Wraps a vanilla {@link Enchantment} and level into an {@link IPluginEnchantment} implementation
     * that supports adding to/removing from items and books.
     *
     * @param enchantment the enchantment to wrap
     * @param level       the enchantment level
     * @return a new {@link IPluginEnchantment} adapter
     */
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
