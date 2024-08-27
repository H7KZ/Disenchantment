package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Map;

public class ExcellentPlugin implements IPlugin {
    public static final String name = "ExcellentEnchants";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> EnchantUtils.add(book, en, l, true));

        return book;
    }

    public ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            EnchantUtils.remove(item, enchantment);
        }

        return item;
    }

    @Override
    public void fetchComplementaryEnchantment(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {}

}
