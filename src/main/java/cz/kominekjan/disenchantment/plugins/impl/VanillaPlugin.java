package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.utils.DisenchantUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.utils.DisenchantUtils.removeStoredEnchantment;

public class VanillaPlugin {
    public static ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> DisenchantUtils.addStoredEnchantment(book, en, l));

        return book;
    }

    public static ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            removeStoredEnchantment(item, enchantment);
        }

        return item;
    }

}
