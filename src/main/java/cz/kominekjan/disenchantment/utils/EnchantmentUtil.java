package cz.kominekjan.disenchantment.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EnchantmentUtil {
    private static final Enchantment[] enchantmentCheck = {
            Enchantment.BINDING_CURSE,
            Enchantment.UNBREAKING,
            Enchantment.MENDING
    };

    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storage)
            storage.removeStoredEnchant(enchantment);
        else meta.removeEnchant(enchantment);

        item.setItemMeta(meta);
    }

    public static Boolean canBeEnchanted(Material material) {
        return canBeEnchanted(new ItemStack(material));
    }

    public static Boolean canBeEnchanted(ItemStack item) {
        return Arrays.stream(enchantmentCheck).anyMatch(e -> e.canEnchantItem(item));
    }
}
