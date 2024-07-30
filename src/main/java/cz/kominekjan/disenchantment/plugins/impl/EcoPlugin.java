package cz.kominekjan.disenchantment.plugins.impl;

import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.utils.DisenchantmentUtils.removeStoredEnchantment;

public class EcoPlugin implements IDisenchantmentPlugin {
    public static final String name = "EcoEnchants";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book;

        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach(builder::addStoredEnchantment);

        book = builder.build();

        return book;
    }

    public ItemStack removeItemEnchantments(ItemStack firstItem) {
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
