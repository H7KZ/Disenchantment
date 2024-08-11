package cz.kominekjan.disenchantment.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.*;

public class EventCheckUtils {
    public static boolean itemCheckForDisabled(ItemStack item) {
        if (getDisabledMaterials().stream().anyMatch(m -> m.getKey().getKey().equalsIgnoreCase(item.getType().getKey().getKey())))
            return true;

        boolean cancel = false;

        for (Map.Entry<Enchantment, Boolean> disabledEnchantment : getDisabledEnchantments().entrySet()) {
            if (disabledEnchantment.getValue()) continue;

            List<Enchantment> itemEnchantments = item.getEnchantments().keySet().stream().toList();

            if (itemEnchantments.contains(disabledEnchantment.getKey())) {
                cancel = true;
                break;
            }
        }

        return cancel;
    }

    public static boolean isEventValidDisenchantItem(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null || secondItem == null) return false;

        if (secondItem.getType() != Material.BOOK) return false;

        if (!secondItem.getEnchantments().isEmpty()) return false;

        if (itemCheckForDisabled(firstItem)) return false;

        return !firstItem.getEnchantments().isEmpty();
    }

    public static boolean splitBookCheckForDisabled(ItemStack item) {
        boolean cancel = false;

        for (Map.Entry<Enchantment, Boolean> disabledEnchantment : getDisabledBookSplittingEnchantments().entrySet()) {
            if (disabledEnchantment.getValue()) continue;

            List<Enchantment> itemEnchantments = item.getEnchantments().keySet().stream().toList();

            if (itemEnchantments.contains(disabledEnchantment.getKey())) {
                cancel = true;
                break;
            }
        }

        return cancel;
    }

    public static boolean isEventValidDisenchantSplitBook(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null || secondItem == null) return false;

        if (firstItem.getType() != Material.ENCHANTED_BOOK) return false;
        if (secondItem.getType() != Material.BOOK) return false;

        if (!secondItem.getEnchantments().isEmpty()) return false;

        if (splitBookCheckForDisabled(firstItem)) return false;

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) firstItem.getItemMeta();
        Map<Enchantment, Integer> enchants = meta.getStoredEnchants();

        return !enchants.isEmpty();
    }
}
