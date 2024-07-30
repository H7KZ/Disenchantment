package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.utils.DisenchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.utils.DisenchantmentUtils.removeStoredEnchantment;

public class VanillaPlugin {
    public static ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> DisenchantmentUtils.addStoredEnchantment(book, en, l));

        return book;
    }

    public static ItemStack removeItemEnchantments(ItemStack firstItem) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (getDisabledEnchantments().containsKey(enchantment))
                continue;

            removeStoredEnchantment(item, enchantment);
        }

        return item;
    }
}
