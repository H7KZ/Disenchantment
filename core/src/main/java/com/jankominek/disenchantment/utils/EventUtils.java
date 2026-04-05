package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class containing event-processing logic for disenchantment and shatterment operations.
 * Determines which enchantments should be transferred based on configuration states.
 */
public class EventUtils {
    /**
     * Event utilities for disenchantment operations (extracting enchantments from items to books).
     */
    public static class Disenchantment {
        /**
         * Determines the enchantments eligible for disenchantment from the first item to a book.
         * Validates that the first item is not an enchanted book, the second is a plain book,
         * the material is not disabled, and no enchantment is in the DISABLE state.
         *
         * @param firstItem  the enchanted item in the first anvil slot
         * @param secondItem the book in the second anvil slot
         * @param withDelete whether to also remove enchantments in the DELETE state
         * @return the list of transferable enchantments, or an empty list if conditions are not met
         */
        public static List<IPluginEnchantment> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return List.of();
            if (firstItem.getType() == Material.AIR || secondItem.getType() == Material.AIR) return List.of();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            if (isMaterialDisabled(firstItem)) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils: rejected — material " + firstItem.getType() + " is disabled by config");
                return List.of();
            }

            List<IPluginEnchantment> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils: rejected — slot1 book is not blank (" + secondEnchants.size() + " enchant(s) present)");
                return List.of();
            }

            List<IPluginEnchantment> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils: rejected — at least one enchantment has state=DISABLE");
                return List.of();
            }

            int beforeFilter = firstEnchants.size();
            for (Map.Entry<String, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            if (DiagnosticUtils.isDebugEnabled() && beforeFilter != firstEnchants.size()) {
                String remaining = firstEnchants.stream().map(e -> e.getKey() + ":" + e.getLevel()).collect(Collectors.joining(", "));
                DiagnosticUtils.debug("DISENCHANT", "EventUtils: config state filtering removed " + (beforeFilter - firstEnchants.size()) + " enchant(s), remaining: [" + remaining + "]");
            }

            return firstEnchants;
        }

        /**
         * Determines the enchantments eligible for disenchantment using a third-party plugin's
         * enchantment provider instead of vanilla enchantments.
         *
         * @param firstItem       the enchanted item in the first anvil slot
         * @param secondItem      the book in the second anvil slot
         * @param withDelete      whether to also remove enchantments in the DELETE state
         * @param activatedPlugin the third-party plugin to use for reading enchantments
         * @return the list of transferable enchantments, or an empty list if conditions are not met
         */
        public static List<IPluginEnchantment> getDisenchantedEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin, World world) {
            if (firstItem == null || secondItem == null) return List.of();
            if (firstItem.getType() == Material.AIR || secondItem.getType() == Material.AIR) return List.of();

            if (firstItem.getType() == Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            if (isMaterialDisabled(firstItem)) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils [" + activatedPlugin.getName() + "]: rejected — material " + firstItem.getType() + " is disabled by config");
                return List.of();
            }

            List<IPluginEnchantment> secondEnchants = activatedPlugin.getItemEnchantments(secondItem, world);

            if (!secondEnchants.isEmpty()) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils [" + activatedPlugin.getName() + "]: rejected — slot1 book is not blank (" + secondEnchants.size() + " enchant(s) present)");
                return List.of();
            }

            List<IPluginEnchantment> firstEnchants = activatedPlugin.getItemEnchantments(firstItem, world);

            if (Disenchantment.isAtLeastOneEnchantmentDisabled(firstEnchants)) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils [" + activatedPlugin.getName() + "]: rejected — at least one enchantment has state=DISABLE");
                return List.of();
            }

            int beforeFilter = firstEnchants.size();
            for (Map.Entry<String, EnchantmentStateType> entry : Config.Disenchantment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            if (DiagnosticUtils.isDebugEnabled() && beforeFilter != firstEnchants.size()) {
                String remaining = firstEnchants.stream().map(e -> e.getKey() + ":" + e.getLevel()).collect(Collectors.joining(", "));
                DiagnosticUtils.debug("DISENCHANT", "EventUtils [" + activatedPlugin.getName() + "]: config state filtering removed " + (beforeFilter - firstEnchants.size()) + " enchant(s), remaining: [" + remaining + "]");
            }

            return firstEnchants;
        }

        /**
         * Finds enchantments from the provided list that are configured for deletion during disenchantment.
         *
         * @param enchantments the enchantments to check
         * @return the list of enchantments marked as DELETE in the disenchantment configuration
         */
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

    /**
     * Event utilities for shatterment operations (splitting enchantments from enchanted books).
     */
    public static class Shatterment {
        /**
         * Determines the enchantments eligible for shatterment from an enchanted book.
         * Requires the first item to be an enchanted book with at least 2 enchantments
         * and the second item to be a plain book.
         *
         * @param firstItem  the enchanted book in the first anvil slot
         * @param secondItem the book in the second anvil slot
         * @param withDelete whether to also remove enchantments in the DELETE state
         * @return the list of splittable enchantments, or an empty list if conditions are not met
         */
        public static List<IPluginEnchantment> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete) {
            if (firstItem == null || secondItem == null) return List.of();
            if (firstItem.getType() == Material.AIR || secondItem.getType() == Material.AIR) return List.of();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            List<IPluginEnchantment> secondEnchants = EnchantmentUtils.getItemEnchantments(secondItem);

            if (!secondEnchants.isEmpty()) {
                DiagnosticUtils.debug("SHATTER", "EventUtils: rejected — slot1 book is not blank (" + secondEnchants.size() + " enchant(s) present)");
                return List.of();
            }

            List<IPluginEnchantment> firstEnchants = EnchantmentUtils.getItemEnchantments(firstItem);

            if (Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) {
                DiagnosticUtils.debug("SHATTER", "EventUtils: rejected — at least one enchantment has state=DISABLE");
                return List.of();
            }

            if (firstEnchants.size() < 2) {
                DiagnosticUtils.debug("SHATTER", "EventUtils: rejected — book has only " + firstEnchants.size() + " eligible enchantment(s) (need ≥2)");
                return List.of();
            }

            int beforeFilter = firstEnchants.size();
            for (Map.Entry<String, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            if (DiagnosticUtils.isDebugEnabled() && beforeFilter != firstEnchants.size()) {
                String remaining = firstEnchants.stream().map(e -> e.getKey() + ":" + e.getLevel()).collect(Collectors.joining(", "));
                DiagnosticUtils.debug("SHATTER", "EventUtils: config state filtering removed " + (beforeFilter - firstEnchants.size()) + " enchant(s), remaining: [" + remaining + "]");
            }

            return firstEnchants;
        }

        /**
         * Determines the enchantments eligible for shatterment using a third-party plugin's
         * enchantment provider instead of vanilla enchantments.
         *
         * @param firstItem       the enchanted book in the first anvil slot
         * @param secondItem      the book in the second anvil slot
         * @param withDelete      whether to also remove enchantments in the DELETE state
         * @param activatedPlugin the third-party plugin to use for reading enchantments
         * @return the list of splittable enchantments, or an empty list if conditions are not met
         */
        public static List<IPluginEnchantment> getShattermentEnchantments(ItemStack firstItem, ItemStack secondItem, boolean withDelete, ISupportedPlugin activatedPlugin, World world) {
            if (firstItem == null || secondItem == null) return List.of();
            if (firstItem.getType() == Material.AIR || secondItem.getType() == Material.AIR) return List.of();

            if (firstItem.getType() != Material.ENCHANTED_BOOK) return List.of();
            if (secondItem.getType() != Material.BOOK) return List.of();

            List<IPluginEnchantment> secondEnchants = activatedPlugin.getItemEnchantments(secondItem, world);

            if (!secondEnchants.isEmpty()) {
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: rejected — slot1 book is not blank (" + secondEnchants.size() + " enchant(s) present)");
                return List.of();
            }

            List<IPluginEnchantment> firstEnchants = activatedPlugin.getItemEnchantments(firstItem, world);

            if (Shatterment.isAtLeastOneEnchantmentDisabled(firstEnchants)) {
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: rejected — at least one enchantment has state=DISABLE");
                return List.of();
            }

            if (firstEnchants.size() < 2) {
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: rejected — book has only " + firstEnchants.size() + " eligible enchantment(s) (need ≥2)");
                return List.of();
            }

            int beforeFilter = firstEnchants.size();
            for (Map.Entry<String, EnchantmentStateType> entry : Config.Shatterment.getEnchantmentStates().entrySet()) {
                String key = entry.getKey();
                EnchantmentStateType state = entry.getValue();

                if (EnchantmentStateType.KEEP.equals(state) || (withDelete && EnchantmentStateType.DELETE.equals(state)))
                    firstEnchants.removeIf(e -> e.getKey().equalsIgnoreCase(key));
            }

            if (DiagnosticUtils.isDebugEnabled() && beforeFilter != firstEnchants.size()) {
                String remaining = firstEnchants.stream().map(e -> e.getKey() + ":" + e.getLevel()).collect(Collectors.joining(", "));
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: config state filtering removed " + (beforeFilter - firstEnchants.size()) + " enchant(s), remaining: [" + remaining + "]");
            }

            return firstEnchants;
        }

        /**
         * Finds enchantments from the provided list that are configured for deletion during shatterment.
         *
         * @param enchantments the enchantments to check
         * @return the list of enchantments marked as DELETE in the shatterment configuration
         */
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
