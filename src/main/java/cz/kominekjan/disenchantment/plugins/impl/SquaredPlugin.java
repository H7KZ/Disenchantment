package cz.kominekjan.disenchantment.plugins.impl;

import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;

public class SquaredPlugin implements IDisenchantmentPlugin {
    public static final String name = "EnchantsSquared";

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        enchantments.forEach((en, l) -> CustomEnchantManager.getInstance().addEnchant(book, en.getKey().getKey(), l));

        return book;
    }

    public ItemStack removeItemEnchantments(ItemStack firstItem) {
        ItemStack item = firstItem.clone();

        for (Map.Entry<Enchantment, Integer> entry : firstItem.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();

            if (getDisabledEnchantments().containsKey(enchantment))
                continue;

            CustomEnchantManager.getInstance().removeEnchant(item, enchantment.getKey().getKey());
        }

        return item;
    }
}
