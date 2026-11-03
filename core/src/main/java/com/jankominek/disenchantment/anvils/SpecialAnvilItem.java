package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * The special anvil item: a plain anvil whose {@link ItemMeta} carries the
 * {@code disenchantment:special_anvil} marker. It is identified by that marker only, never by name
 * or lore, so renaming it in an anvil does not matter and the configured look can change freely.
 */
public class SpecialAnvilItem {
    private static final String KEY_NAME = "special_anvil";

    private SpecialAnvilItem() {
    }

    private static NamespacedKey key() {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    /**
     * Creates the special anvil item in its undamaged state.
     *
     * @param amount the stack size
     * @return a new special anvil stack
     */
    public static ItemStack create(int amount) {
        return create(Material.ANVIL, amount);
    }

    /**
     * Creates the special anvil item in the given damage state.
     *
     * @param material one of the three anvil materials
     * @param amount   the stack size
     * @return a new special anvil stack
     */
    public static ItemStack create(Material material, int amount) {
        ItemStack item = new ItemStack(SpecialAnvilStore.isAnvilMaterial(material) ? material : Material.ANVIL, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(color(Config.SpecialAnvils.getItemName()));

        List<String> lore = new ArrayList<>();
        for (String line : Config.SpecialAnvils.getItemLore()) lore.add(color(line));
        if (!lore.isEmpty()) meta.setLore(lore);

        meta.getPersistentDataContainer().set(key(), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Returns whether the stack is a special anvil item.
     *
     * @param item the stack to test (may be {@code null})
     * @return {@code true} if the stack is an anvil carrying the special marker
     */
    public static boolean isSpecial(ItemStack item) {
        if (item == null || !SpecialAnvilStore.isAnvilMaterial(item.getType())) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer().has(key(), PersistentDataType.BYTE);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
