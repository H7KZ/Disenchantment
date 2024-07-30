package cz.kominekjan.disenchantment.plugins;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface IDisenchantmentPlugin {
    String name = "Unknown";

    ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments);

    ItemStack removeItemEnchantments(ItemStack firstItem);
}
