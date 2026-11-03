package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class containing event-processing logic for disenchantment and shatterment operations.
 * Determines which enchantments should be transferred based on configuration states.
 */
public class EventUtils {
    // ThreadLocal so concurrent Folia region threads each get their own flag.
    private static final ThreadLocal<Boolean> materialBypassActive = ThreadLocal.withInitial(() -> false);

    /**
     * Activates or deactivates the material-bypass flag for the current thread.
     * When active, disabled-material checks are skipped (used by bypass-permission code paths).
     *
     * @param bypass {@code true} to bypass material restrictions on the current thread
     */
    public static void setMaterialBypass(boolean bypass) {
        materialBypassActive.set(bypass);
    }

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
            if (!isBlankBook(secondItem)) return List.of();

            if (firstItem.getAmount() > 1) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils: rejected — source item is a stack (amount " + firstItem.getAmount() + " > 1); disenchantment operates on a single item");
                return List.of();
            }

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
                    firstEnchants.removeIf(e -> normalizeKey(e.getKey()).equalsIgnoreCase(normalizeKey(key)));
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
            if (!isBlankBook(secondItem)) return List.of();

            if (firstItem.getAmount() > 1) {
                DiagnosticUtils.debug("DISENCHANT", "EventUtils [" + activatedPlugin.getName() + "]: rejected — source item is a stack (amount " + firstItem.getAmount() + " > 1); disenchantment operates on a single item");
                return List.of();
            }

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
                    firstEnchants.removeIf(e -> normalizeKey(e.getKey()).equalsIgnoreCase(normalizeKey(key)));
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
            Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                if (getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.DELETE)
                    result.add(enchantment);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(List<IPluginEnchantment> enchantments) {
            Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                if (getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.DISABLE) return true;
            }

            return false;
        }

        private static boolean isMaterialDisabled(ItemStack item) {
            if (materialBypassActive.get()) return false;
            return Config.Disenchantment.isMaterialRestricted(item.getType());
        }
    }

    /**
     * Event utilities for shatterment operations (splitting enchantments from enchanted books).
     */
    public static class Shatterment {
        /**
         * Returns {@code true} when the source book carries at least one enchantment in the KEEP
         * state. Such enchantments always stay on the source, so every splittable enchantment may
         * move to the result without leaving the source book empty.
         *
         * @param book the enchanted book being shattered
         * @return whether a KEEP-state enchantment remains on the source after splitting
         */
        public static boolean hasKeptEnchantment(ItemStack book) {
            if (book == null) return false;
            Map<String, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();
            if (states.isEmpty()) return false;
            return EnchantmentUtils.getItemEnchantments(book).stream()
                    .anyMatch(e -> EnchantmentStateType.KEEP.equals(getStateForKey(states, e.getKey())));
        }

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
            if (!isBlankBook(secondItem)) return List.of();

            if (firstItem.getAmount() > 1) {
                DiagnosticUtils.debug("SHATTER", "EventUtils: rejected — source book is a stack (amount " + firstItem.getAmount() + " > 1); shatterment operates on a single item");
                return List.of();
            }

            if (isMaterialDisabled(firstItem)) {
                DiagnosticUtils.debug("SHATTER", "EventUtils: rejected — material " + firstItem.getType() + " is disabled by config");
                return List.of();
            }

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
                    firstEnchants.removeIf(e -> normalizeKey(e.getKey()).equalsIgnoreCase(normalizeKey(key)));
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
            if (!isBlankBook(secondItem)) return List.of();

            if (firstItem.getAmount() > 1) {
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: rejected — source book is a stack (amount " + firstItem.getAmount() + " > 1); shatterment operates on a single item");
                return List.of();
            }

            if (isMaterialDisabled(firstItem)) {
                DiagnosticUtils.debug("SHATTER", "EventUtils [" + activatedPlugin.getName() + "]: rejected — material " + firstItem.getType() + " is disabled by config");
                return List.of();
            }

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
                    firstEnchants.removeIf(e -> normalizeKey(e.getKey()).equalsIgnoreCase(normalizeKey(key)));
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
            Map<String, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                if (getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.DELETE)
                    result.add(enchantment);
            }

            return result;
        }

        private static boolean isAtLeastOneEnchantmentDisabled(List<IPluginEnchantment> enchantments) {
            Map<String, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();

            for (IPluginEnchantment enchantment : enchantments) {
                if (getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.DISABLE) return true;
            }

            return false;
        }

        private static boolean isMaterialDisabled(ItemStack item) {
            if (materialBypassActive.get()) return false;
            return Config.Shatterment.isMaterialRestricted(item.getType());
        }
    }

    /**
     * Returns {@code true} when {@code item} is a plain, unmodified {@link Material#BOOK} that may
     * serve as the blank book of an operation. A book another plugin has tagged as something special
     * (custom display name, lore, custom model data, or any persistent-data key) is rejected so a
     * quest/currency/voucher book cannot be consumed by a disenchant or shatter operation.
     *
     * @param item the item in the second anvil slot
     * @return whether the item is a vanilla-looking blank book
     */
    public static boolean isBlankBook(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK) return false;
        if (!item.hasItemMeta()) return true;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        if (meta.hasDisplayName() || meta.hasLore() || meta.hasCustomModelData()) return false;
        return meta.getPersistentDataContainer().getKeys().isEmpty();
    }

    /**
     * Normalized (namespace-stripped, lower-case) lookup key for an enchantment key, so vanilla
     * ({@code sharpness}), namespaced ({@code minecraft:sharpness}) and adapter keys compare equal.
     */
    private static String lookupKey(String key) {
        return normalizeKey(key == null ? "" : key).toLowerCase();
    }

    /**
     * Returns the subset of {@code collected} that the anvil result book actually carries — i.e. the
     * enchantments that are really transferred to the player. This is the one set both the action-bar
     * cost display (Prepare) and the economy charge (Click) must be based on: the click-time
     * collection also contains enchantments that never reach the book (DELETE-state ones, or the
     * shatter candidates not picked for this split).
     * <p>
     * The result book is read the same way the click handlers read it to strip the source item:
     * vanilla stored enchantments plus every activated adapter's view of the book.
     *
     * @param collected     the enchantments collected for the operation (order is preserved)
     * @param resultBook    the item in the anvil result slot
     * @param activatedPlugins the activated third-party adapters
     * @param world         the player's world (passed through to adapters)
     * @return the entries of {@code collected} whose key is present on the result book
     */
    public static List<IPluginEnchantment> findTransferredEnchantments(
            List<IPluginEnchantment> collected,
            ItemStack resultBook,
            List<ISupportedPlugin> activatedPlugins,
            World world) {
        Set<String> onBook = new HashSet<>();
        if (resultBook != null && resultBook.getType() != Material.AIR) {
            for (IPluginEnchantment ench : EnchantmentUtils.getItemEnchantments(resultBook)) {
                onBook.add(lookupKey(ench.getKey()));
            }
            for (ISupportedPlugin plugin : activatedPlugins) {
                for (IPluginEnchantment ench : plugin.getItemEnchantments(resultBook, world)) {
                    onBook.add(lookupKey(ench.getKey()));
                }
            }
        }

        List<IPluginEnchantment> transferred = new ArrayList<>();
        for (IPluginEnchantment ench : collected) {
            if (onBook.contains(lookupKey(ench.getKey()))) transferred.add(ench);
        }
        return transferred;
    }

    /**
     * Returns the entries of {@code enchantments} whose key also appears in {@code reference}
     * (keys compared normalized). Used to keep the economy charge limited to the transferred set
     * after third-party listeners had a chance to edit the Pre* event's enchantment list.
     */
    public static List<IPluginEnchantment> retainByKey(List<IPluginEnchantment> enchantments, List<IPluginEnchantment> reference) {
        Set<String> keys = new HashSet<>();
        for (IPluginEnchantment ench : reference) keys.add(lookupKey(ench.getKey()));

        List<IPluginEnchantment> retained = new ArrayList<>();
        for (IPluginEnchantment ench : enchantments) {
            if (keys.contains(lookupKey(ench.getKey()))) retained.add(ench);
        }
        return retained;
    }

    // Strips namespace prefix (e.g. "minecraft:mending" → "mending") so config keys written
    // with or without a namespace both match server-returned short keys.
    public static String normalizeKey(String key) {
        int colon = key.indexOf(':');
        return colon >= 0 ? key.substring(colon + 1) : key;
    }

    public static EnchantmentStateType getStateForKey(Map<String, EnchantmentStateType> states, String enchantmentKey) {
        String normalized = normalizeKey(enchantmentKey);
        for (Map.Entry<String, EnchantmentStateType> entry : states.entrySet()) {
            if (normalizeKey(entry.getKey()).equalsIgnoreCase(normalized)) return entry.getValue();
        }
        return EnchantmentStateType.ENABLE;
    }
}
