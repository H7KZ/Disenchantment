package cz.kominekjan.disenchantment.utils;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.types.EnchantmentState;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

public class EventCheckUtils {
    public static class Disenchantment {
        public static Map<Enchantment, Integer> getValidEnchantments(ItemStack firstItem, ItemStack secondItem) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            if (isMaterialDisabled(firstItem)) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = DisenchantUtils.fetchEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = DisenchantUtils.fetchEnchantments(firstItem);

            Config.Disenchantment.getEnchantmentStates().forEach((enchantment, state) -> {
                if (EnchantmentState.KEEP.equals(state)) firstEnchants.remove(enchantment);
            });

            if (EventCheckUtils.Disenchantment.areEnchantmentsDisabled(firstEnchants)) return Collections.emptyMap();

            return firstEnchants;
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
        public static Map<Enchantment, Integer> getValidEnchantments(ItemStack firstItem, ItemStack secondItem) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = DisenchantUtils.fetchEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = DisenchantUtils.fetchEnchantments(firstItem);

            if (EventCheckUtils.Shatterment.areEnchantmentsDisabled(firstEnchants)) return Collections.emptyMap();

            if (firstEnchants.size() < 2) return Collections.emptyMap();

            Config.Shatterment.getEnchantmentStates().forEach((enchantment, state) -> {
                if (EnchantmentState.KEEP.equals(state)) firstEnchants.remove(enchantment);
            });

            return firstEnchants;
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
