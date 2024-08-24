package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SquaredPlugin implements IPlugin {
    public static final String name = "EnchantsSquared";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> CustomEnchantManager.getInstance().addEnchant(book, en.getKey().getKey(), l));

        return book;
    }

    public ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            CustomEnchantManager.getInstance().removeEnchant(item, enchantment.getKey().getKey());
        }

        return item;
    }

}