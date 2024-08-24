package cz.kominekjan.disenchantment.utils;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.types.EnchantmentState;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class EventCheckUtils {
    public static class Disenchantment {
        public static Boolean isEventValid(ItemStack firstItem, ItemStack secondItem) {
            if (firstItem == null || secondItem == null) return false;

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return false;
            if (secondItem.getType() != Material.BOOK) return false;

            if (!secondItem.getEnchantments().isEmpty()) return false;

            if (isMaterialDisabled(firstItem)) return false;

            if (EventCheckUtils.Disenchantment.areEnchantmentsDisabled(firstItem.getEnchantments())) return false;

            return !firstItem.getEnchantments().isEmpty();
        }

        private static Boolean areEnchantmentsDisabled(Map<Enchantment, Integer> enchantments) {
            Map<Enchantment, EnchantmentState> states = Config.Shatterment.getEnchantmentStates();

            for (Enchantment enchantment : enchantments.keySet()) {
                if (states.getOrDefault(enchantment, EnchantmentState.ENABLED) == EnchantmentState.DISABLED)
                    return true;
            }

            return false;
        }

        private static Boolean isMaterialDisabled(ItemStack item) {
            return Config.Disenchantment.getDisabledMaterials().stream().anyMatch(m -> m.equals(item.getType()));
        }
    }

    public static class Shatterment {
        public static Boolean isEventValid(ItemStack firstItem, ItemStack secondItem) {
            if (firstItem == null || secondItem == null) return false;

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return false;
            if (secondItem.getType() != Material.BOOK) return false;

            if (!secondItem.getEnchantments().isEmpty()) return false;

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) firstItem.getItemMeta();
            Map<Enchantment, Integer> enchants = meta.getStoredEnchants();

            if (EventCheckUtils.Shatterment.areEnchantmentsDisabled(enchants)) return false;

            return !enchants.isEmpty();
        }

        private static Boolean areEnchantmentsDisabled(Map<Enchantment, Integer> enchantments) {
            Map<Enchantment, EnchantmentState> states = Config.Shatterment.getEnchantmentStates();

            for (Enchantment enchantment : enchantments.keySet()) {
                if (states.getOrDefault(enchantment, EnchantmentState.ENABLED) == EnchantmentState.DISABLED)
                    return true;
            }

            return false;
        }
    }
}
