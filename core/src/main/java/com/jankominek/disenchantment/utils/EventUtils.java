package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventUtils {
    public static class Disenchantment {
        public static List<IPluginEnchantment> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return List.of();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            if (isMaterialDisabled(firstItem)) return List.of();

            List<IPluginEnchantment> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return List.of();

            List<IPluginEnchantment> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return List.of();

            for (Map.Entry<String, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            return firstEnchants;
        }

        public static List<IPluginEnchantment> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin) {
            if (firstItem == null || secondItem == null) return List.of();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            if (isMaterialDisabled(firstItem)) return List.of();

            List<IPluginEnchantment> secondEnchants = activatedPlugin.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return List.of();

            List<IPluginEnchantment> firstEnchants = activatedPlugin.getItemEnchantments(firstItem);

            if (Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return List.of();

            for (Map.Entry<String, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            return firstEnchants;
        }

        public static List<IPluginEnchantment> findEnchantmentsToDelete(List<IPluginEnchantment> enchantments) {
            List<IPluginEnchantment> result = new ArrayList<>();

            for (IPluginEnchantment enchantment : enchantments) {
                String key = enchantment.getKey();

                if (Config.Disenchantment.getEnchantmentStates().getOrDefault(key, EnchantmentStateType.ENABLE) == EnchantmentStateType.DELETE)
                    result.add(enchantment);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(List<IPluginEnchantment> enchantments) {
            Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                String key = enchantment.getKey();

                if (states.getOrDefault(key, EnchantmentStateType.ENABLE) == EnchantmentStateType.DISABLE) return true;
            }

            return false;
        }

        private static boolean isMaterialDisabled(ItemStack item) {
            return Config.Disenchantment.getDisabledMaterials().stream().anyMatch(m -> m.equals(item.getType()));
        }
    }

    public static class Shatterment {
        public static List<IPluginEnchantment> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return List.of();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            List<IPluginEnchantment> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return List.of();

            List<IPluginEnchantment> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return List.of();

            if (firstEnchants.size() < 2) return List.of();

            for (Map.Entry<String, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            return firstEnchants;
        }

        public static List<IPluginEnchantment> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin) {
            if (firstItem == null || secondItem == null) return List.of();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            List<IPluginEnchantment> secondEnchants = activatedPlugin.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) return List.of();

            List<IPluginEnchantment> firstEnchants = activatedPlugin.getItemEnchantments(firstItem);

            if (Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) return List.of();

            if (firstEnchants.size() < 2) return List.of();

            for (Map.Entry<String, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            return firstEnchants;
        }

        public static List<IPluginEnchantment> findEnchantmentsToDelete(List<IPluginEnchantment> enchantments) {
            List<IPluginEnchantment> result = new ArrayList<>();

            for (IPluginEnchantment enchantment : enchantments) {
                String key = enchantment.getKey();

                if (Config.Shatterment.getEnchantmentStates().getOrDefault(key, EnchantmentStateType.ENABLE) == EnchantmentStateType.DELETE)
                    result.add(enchantment);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(List<IPluginEnchantment> enchantments) {
            Map<String, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                String key = enchantment.getKey();

                if (states.getOrDefault(key, EnchantmentStateType.ENABLE) == EnchantmentStateType.DISABLE) return true;
            }

            return false;
        }
    }
}
