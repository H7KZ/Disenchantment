package cz.kominekjan.disenchantment.utils;

import cz.kominekjan.disenchantment.config.EnchantmentStatus;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.*;

public class EventCheckUtils {

    public static boolean checkForDisabledEnchantments(ItemStack item, Map<Enchantment, EnchantmentStatus> statuses) {
        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            EnchantmentStatus status = statuses.getOrDefault(enchantment, EnchantmentStatus.ENABLED);

            if(status == EnchantmentStatus.DISABLED) return true;
        }

        return false;
    }

    public static boolean itemCheckForDisabled(ItemStack item) {
        if (getDisabledMaterials().stream().anyMatch(m -> m.equals(item.getType())))
            return true;

        return checkForDisabledEnchantments(item, getEnchantmentsStatus());
    }

    public static boolean isEventValidDisenchantItem(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null || secondItem == null) return false;

        if (secondItem.getType() != Material.BOOK) return false;

        if (!secondItem.getEnchantments().isEmpty()) return false;

        if (itemCheckForDisabled(firstItem)) return false;

        return !firstItem.getEnchantments().isEmpty();
    }

    public static boolean splitBookCheckForDisabled(ItemStack item) {
        return checkForDisabledEnchantments(item, getBookSplittingEnchantmentsStatus());
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
