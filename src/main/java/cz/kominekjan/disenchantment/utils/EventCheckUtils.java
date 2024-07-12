package cz.kominekjan.disenchantment.utils;

import cz.kominekjan.disenchantment.types.DisabledEnchantment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.getDisabledMaterials;

public class EventCheckUtils {
    public static boolean checkForDisabled(ItemStack item) {
        if (getDisabledMaterials().stream().anyMatch(m -> m.getKey().getKey().equalsIgnoreCase(item.getType().getKey().getKey())))
            return true;

        boolean cancel = false;

        for (DisabledEnchantment disabledEnchantment : getDisabledEnchantments()) {
            if (disabledEnchantment.doKeep()) continue;

            List<String> itemEnchantments = item.getEnchantments().keySet().stream().map(Enchantment::getKey).map(NamespacedKey::getKey).toList();

            if (itemEnchantments.stream().anyMatch(m -> m.equalsIgnoreCase(disabledEnchantment.getEnchantmentKey()))) {
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
