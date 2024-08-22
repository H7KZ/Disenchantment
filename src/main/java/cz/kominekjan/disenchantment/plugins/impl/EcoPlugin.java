package cz.kominekjan.disenchantment.plugins.impl;

import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import cz.kominekjan.disenchantment.plugins.IPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.utils.DisenchantUtils.removeStoredEnchantment;

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
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            removeStoredEnchantment(item, enchantment);
        }

        return item;
    }

}
