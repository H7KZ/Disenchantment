package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;

public class AdvancedPlugin implements IDisenchantmentPlugin {
    public static final String name = "AdvancedEnchantments";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> {
            AEAPI.applyEnchant(en.getKey().getKey(), l, book, true);
        });
        return book;
    }

    public ItemStack removeItemEnchantments(ItemStack firstItem) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (getDisabledEnchantments().containsKey(enchantment))
                continue;

            AEAPI.removeEnchantment(item, enchantment.getKey().getKey());
        }

        return item;
    }
}
