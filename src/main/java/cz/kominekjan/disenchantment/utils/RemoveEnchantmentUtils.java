package cz.kominekjan.disenchantment.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class RemoveEnchantmentUtils {
    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storage)
            storage.removeStoredEnchant(enchantment);
        else meta.removeEnchant(enchantment);
        item.setItemMeta(meta);
    }
}
