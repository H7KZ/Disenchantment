package cz.kominekjan.disenchantment.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.getDisabledMaterials;

public class EventCheckUtils {
    public static boolean checkForDisabled(ItemStack item) {
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

    public static boolean isEventValidDisenchantment(ItemStack firstItem, ItemStack secondItem) {
        if (firstItem == null || secondItem == null) return false;

        if (secondItem.getType() != Material.BOOK) return false;

        if (!secondItem.getEnchantments().isEmpty()) return false;

        if (checkForDisabled(firstItem)) return false;

        return !firstItem.getEnchantments().isEmpty();
    }
}
