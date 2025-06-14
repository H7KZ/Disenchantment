package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventUtils {
    public static class Disenchantment {
        public static Map<Enchantment, Integer> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            if (isMaterialDisabled(firstItem)) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (EventUtils.Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return Collections.emptyMap();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                Enchantment enchantment = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.remove(enchantment);
            }

            return firstEnchants;
        }

        public static Map<Enchantment, Integer> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            if (isMaterialDisabled(firstItem)) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = activatedPlugin.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = activatedPlugin.getItemEnchantments(firstItem);

            if (EventUtils.Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return Collections.emptyMap();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                Enchantment enchantment = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.remove(enchantment);
            }

            return firstEnchants;
        }

        public static Map<Enchantment, Integer> findEnchantmentsToDelete(Map<Enchantment, Integer> enchantments) {
            HashMap<Enchantment, Integer> result = new HashMap<>();

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                if (Config.Disenchantment.getEnchantmentStates().getOrDefault(enchantment, EnchantmentStateType.ENABLE) == EnchantmentStateType.DELETE)
                    result.put(enchantment, level);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(Map<Enchantment, Integer> enchantments) {
            Map<Enchantment, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

            for (Enchantment enchantment : enchantments.keySet()) {
                if (states.getOrDefault(enchantment, EnchantmentStateType.ENABLE) == EnchantmentStateType.DISABLE)
                    return true;
            }

            return false;
        }

        private static boolean isMaterialDisabled(ItemStack item) {
            return Config.Disenchantment.getDisabledMaterials().stream().anyMatch(m -> m.equals(item.getType()));
        }
    }

    public static class Shatterment {
        public static Map<Enchantment, Integer> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (EventUtils.Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return Collections.emptyMap();

            if (firstEnchants.size() < 2) return Collections.emptyMap();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                Enchantment enchantment = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.remove(enchantment);
            }

            return firstEnchants;
        }

        public static Map<Enchantment, Integer> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin) {
            if (firstItem == null || secondItem == null) return Collections.emptyMap();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return Collections.emptyMap();
            if (secondItem.getType() != Material.BOOK) return Collections.emptyMap();

            Map<Enchantment, Integer> secondEnchants = activatedPlugin.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return Collections.emptyMap();

            Map<Enchantment, Integer> firstEnchants = activatedPlugin.getItemEnchantments(firstItem);

            if (EventUtils.Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return Collections.emptyMap();

            if (firstEnchants.size() < 2) return Collections.emptyMap();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                Enchantment enchantment = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.remove(enchantment);
            }

            return firstEnchants;
        }

        public static Map<Enchantment, Integer> findEnchantmentsToDelete(Map<Enchantment, Integer> enchantments) {
            HashMap<Enchantment, Integer> result = new HashMap<>();

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                if (Config.Shatterment.getEnchantmentStates().getOrDefault(enchantment, EnchantmentStateType.ENABLE) == EnchantmentStateType.DELETE)
                    result.put(enchantment, level);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(Map<Enchantment, Integer> enchantments) {
            Map<Enchantment, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();

            for (Enchantment enchantment : enchantments.keySet()) {
                if (states.getOrDefault(enchantment, EnchantmentStateType.ENABLE) == EnchantmentStateType.DISABLE)
                    return true;
            }

            return false;
        }
    }
}
