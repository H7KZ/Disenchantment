package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Pure logic of erase mode: which catalysts are accepted and which enchantments an item loses.
 * <p>
 * Worlds, materials and enchantment states are shared with disenchantment:
 * {@link EnchantmentStateType#KEEP} enchantments stay on the item, a {@link EnchantmentStateType#DISABLE}
 * enchantment refuses the whole operation, everything else (including {@code DELETE}) is erased.
 * Curses stay unless {@code erase.erase-curses} is on. {@code enchantment-chances} do not apply (there is
 * no book to fail onto).
 */
public final class EraseUtils {

    private EraseUtils() {
    }

    // ----------------------------------------------------------------------------------------------------
    // Catalysts

    // Keyed by the raw configured list, so a config change is picked up without a reset and the
    // parse warnings are logged once per distinct list, not on every anvil keystroke.
    private static final Map<List<String>, Set<Material>> catalystCache = new ConcurrentHashMap<>();

    /**
     * Parses catalyst names with {@link Material#matchMaterial(String)} (a {@code minecraft:} prefix is
     * accepted). Unknown names, {@code BOOK}, {@code ENCHANTED_BOOK} and air are skipped with a warning —
     * the books belong to disenchantment and shatterment, so the handlers stay mutually exclusive.
     *
     * @param names the configured material names
     * @return the accepted materials in config order, without duplicates; empty when nothing usable is left
     */
    public static Set<Material> parseCatalysts(List<String> names) {
        Set<Material> materials = new LinkedHashSet<>();
        if (names == null) return materials;

        for (String name : names) {
            Material material = name == null ? null : Material.matchMaterial(name.trim());

            if (material == null) {
                logger.warning("Config: erase.catalysts contains the unknown material '" + name + "' — skipped.");
                continue;
            }
            if (material == Material.BOOK || material == Material.ENCHANTED_BOOK || material.isAir()) {
                logger.warning("Config: erase.catalysts must not contain " + material.name()
                        + " (books are used by disenchantment and shatterment, air is not an item) — skipped.");
                continue;
            }

            materials.add(material);
        }

        return materials;
    }

    /**
     * Returns the accepted catalyst materials of the current config. Warns once when none are usable.
     *
     * @return the accepted catalysts, possibly empty
     */
    public static Set<Material> getCatalysts() {
        List<String> raw = List.copyOf(Config.Erase.getCatalysts());

        Set<Material> cached = catalystCache.get(raw);
        if (cached != null) return cached;

        Set<Material> parsed = Set.copyOf(parseCatalysts(raw));
        if (catalystCache.putIfAbsent(raw, parsed) == null && parsed.isEmpty()) {
            logger.warning("Config: erase.catalysts has no usable material — erase mode is disabled.");
        }

        return catalystCache.get(raw);
    }

    /**
     * Forgets the cached catalyst parse (and with it the "already warned" state). Intended for tests.
     */
    public static void clearCache() {
        catalystCache.clear();
    }

    /**
     * Returns whether erase mode is on and has at least one usable catalyst.
     *
     * @return {@code true} if the erase handlers should act
     */
    public static boolean isActive() {
        return Config.Erase.isEnabled() && !getCatalysts().isEmpty();
    }

    /**
     * Logs the catalyst warnings at startup/reload when erase mode is enabled, instead of on the first anvil use.
     */
    public static void validateConfig() {
        if (Config.Erase.isEnabled()) getCatalysts();
    }

    /**
     * Returns whether {@code item} is an accepted catalyst. Only the material is compared: a renamed or
     * custom-model catalyst still counts.
     *
     * @param item the item in the second anvil slot
     * @return {@code true} if it is a catalyst
     */
    public static boolean isCatalyst(ItemStack item) {
        return item != null && !item.getType().isAir() && getCatalysts().contains(item.getType());
    }

    // ----------------------------------------------------------------------------------------------------
    // Erasable enchantments

    /**
     * Determines the enchantments erase mode removes from {@code first}, using vanilla enchantments.
     * Empty when the inputs are not a valid erase: the second item is no catalyst, the first is a book or
     * a stack, its material is restricted, or one of its enchantments is in the DISABLE state.
     *
     * @param first  the enchanted item in anvil slot 0
     * @param second the catalyst in anvil slot 1
     * @return the enchantments to erase, possibly empty
     */
    public static List<IPluginEnchantment> getErasableEnchantments(ItemStack first, ItemStack second) {
        if (!isValidInput(first, second, "")) return List.of();

        return filter(EnchantmentUtils.getItemEnchantments(first), "");
    }

    /**
     * Same as {@link #getErasableEnchantments(ItemStack, ItemStack)}, shaped for
     * {@code AnvilEventGuards.EnchantmentCollector}. {@code isPrepare} makes no difference: DELETE-state
     * enchantments are erased in both phases.
     */
    public static List<IPluginEnchantment> getErasableEnchantments(ItemStack first, ItemStack second, boolean isPrepare) {
        return getErasableEnchantments(first, second);
    }

    /**
     * Determines the enchantments erase mode removes from {@code first}, reading them through a
     * third-party plugin's provider instead of vanilla enchantments.
     *
     * @param first           the enchanted item in anvil slot 0
     * @param second          the catalyst in anvil slot 1
     * @param isPrepare       unused, see {@link #getErasableEnchantments(ItemStack, ItemStack, boolean)}
     * @param activatedPlugin the third-party plugin to read enchantments from
     * @param world           the player's world
     * @return the enchantments to erase, possibly empty
     */
    public static List<IPluginEnchantment> getErasableEnchantments(ItemStack first, ItemStack second, boolean isPrepare, ISupportedPlugin activatedPlugin, World world) {
        String tag = " [" + activatedPlugin.getName() + "]";
        if (!isValidInput(first, second, tag)) return List.of();

        return filter(new ArrayList<>(activatedPlugin.getItemEnchantments(first, world)), tag);
    }

    private static boolean isValidInput(ItemStack first, ItemStack second, String tag) {
        if (first == null || second == null) return false;
        if (first.getType().isAir() || second.getType().isAir()) return false;

        if (first.getType() == Material.ENCHANTED_BOOK || first.getType() == Material.BOOK) return false;

        if (!isCatalyst(second)) return false;

        if (first.getAmount() > 1) {
            DiagnosticUtils.debug("ERASE", "EraseUtils" + tag + ": rejected — source item is a stack (amount " + first.getAmount() + " > 1)");
            return false;
        }

        if (!EventUtils.isMaterialBypassActive() && Config.Disenchantment.isMaterialRestricted(first.getType())) {
            DiagnosticUtils.debug("ERASE", "EraseUtils" + tag + ": rejected — material " + first.getType() + " is restricted by config");
            return false;
        }

        return true;
    }

    private static List<IPluginEnchantment> filter(List<IPluginEnchantment> enchantments, String tag) {
        Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

        for (IPluginEnchantment enchantment : enchantments) {
            if (EventUtils.getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.DISABLE) {
                DiagnosticUtils.debug("ERASE", "EraseUtils" + tag + ": rejected — " + enchantment.getKey() + " has state=DISABLE");
                return List.of();
            }
        }

        boolean eraseCurses = Config.Erase.isEraseCurses();
        List<IPluginEnchantment> erasable = new ArrayList<>();

        for (IPluginEnchantment enchantment : enchantments) {
            if (EventUtils.getStateForKey(states, enchantment.getKey()) == EnchantmentStateType.KEEP) continue;
            if (!eraseCurses && isCurse(enchantment)) continue;

            erasable.add(enchantment);
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = erasable.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("ERASE", "EraseUtils" + tag + ": erasable=[" + names + "]");
        }

        return erasable;
    }

    /**
     * Returns whether {@code enchantment} is a curse: its key ends in {@code _curse}, or, for a vanilla
     * enchantment, {@link Enchantment#isCursed()} says so.
     *
     * @param enchantment the enchantment to test
     * @return {@code true} for curses
     */
    public static boolean isCurse(IPluginEnchantment enchantment) {
        String key = enchantment.getKey();
        if (key == null) return false;

        String normalized = EventUtils.normalizeKey(key).toLowerCase();
        if (normalized.endsWith("_curse")) return true;

        // Only a namespace-less (vanilla) key is looked up in the registry.
        if (key.indexOf(':') >= 0 && !key.toLowerCase().startsWith("minecraft:")) return false;

        try {
            Enchantment vanilla = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(normalized));
            return vanilla != null && vanilla.isCursed();
        } catch (RuntimeException | LinkageError e) {
            // isCursed() is deprecated and moved to tags on newer APIs; the key check above already covers vanilla curses.
            return false;
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // Result

    /**
     * Builds the erase result: a copy of {@code first} without {@code toErase}. Name, lore, durability and
     * persistent data are kept.
     *
     * @param first   the item in anvil slot 0 (not modified)
     * @param toErase the enchantments to remove
     * @return the new item
     */
    public static ItemStack buildResult(ItemStack first, List<IPluginEnchantment> toErase) {
        ItemStack result = first.clone();

        for (IPluginEnchantment enchantment : toErase) {
            result = enchantment.removeFromItem(result);
        }

        return result;
    }
}
