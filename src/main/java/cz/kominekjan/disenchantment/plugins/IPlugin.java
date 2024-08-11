package cz.kominekjan.disenchantment.plugins;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface IPlugin {
    String name = "Unknown";

    ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments);

    ItemStack removeEnchantments(ItemStack firstItem, Map<Enchantment, Integer> enchantments);

    ItemStack removeAllEnchantments(ItemStack firstItem);
}
