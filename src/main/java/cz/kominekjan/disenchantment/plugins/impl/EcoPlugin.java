package cz.kominekjan.disenchantment.plugins.impl;

import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import cz.kominekjan.disenchantment.plugins.IPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EcoPlugin implements IPlugin {
    public static final String name = "EcoEnchants";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book;

        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach(builder::addStoredEnchantment);

        book = builder.build();

        return book;
    }

    public ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        return VanillaPlugin.removeEnchantments(firstItem, enchantments);
    }

}
