package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;

public class ExcellentPlugin implements IDisenchantmentPlugin {
    public static final String name = "ExcellentEnchants";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> EnchantUtils.add(book, en, l, true));

        return book;
    }

    public ItemStack removeItemEnchantments(ItemStack firstItem) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (getDisabledEnchantments().containsKey(enchantment))
                continue;

            EnchantUtils.remove(item, enchantment);
        }

        return item;
    }
}
