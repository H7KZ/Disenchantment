package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.types.PermissionType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Pure computation shared by the grindstone prepare and click handlers: which enchantments an
 * extraction or a transfer moves, and the items that result. Nothing here touches an inventory or a
 * player's balance, so the prepare preview and the click always agree.
 */
public final class GrindstoneOps {
    private GrindstoneOps() {
    }

    // ----------------------------------------------------------------------------------------------------
    // Shared helpers

    /**
     * Returns {@code true} when the enchantment is a curse (vanilla {@code binding_curse},
     * {@code vanishing_curse}, or any adapter key ending in {@code _curse}).
     */
    public static boolean isCurse(IPluginEnchantment enchantment) {
        return isCurseKey(enchantment.getKey());
    }

    private static boolean isCurseKey(String key) {
        return key != null && EventUtils.normalizeKey(key).toLowerCase().endsWith("_curse");
    }

    // ----------------------------------------------------------------------------------------------------
    // Extraction (enchanted item + plain book -> enchanted book)

    /**
     * Collects the enchantments an extraction would move from {@code source} onto the blank book
     * {@code book}. Goes through the same pipeline as the anvil disenchantment (world/material
     * restrictions, enchantment states, plugin adapters) and then drops curses unless
     * {@code grindstone.curses} is enabled — a curse stays on the item exactly like in the vanilla grindstone.
     *
     * @param source    the item in grindstone slot 0
     * @param book      the item in grindstone slot 1
     * @param withDelete {@code true} on prepare (DELETE-state enchantments are excluded from the book),
     *                   {@code false} on click (they are kept in the list so they can be destroyed on the source)
     * @param world     the player's world
     * @param player    the player (honours {@code disenchantment.bypass.material})
     * @return the enchantments to move, empty when the combination is not an extraction
     */
    public static List<IPluginEnchantment> collectExtract(ItemStack source, ItemStack book, boolean withDelete, World world, Player player) {
        List<IPluginEnchantment> collected = AnvilEventGuards.collectEnchantments(
                source, book, withDelete,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                world, player);

        if (Config.Grindstone.isCursesEnabled()) return collected;

        List<IPluginEnchantment> result = new ArrayList<>(collected);
        result.removeIf(GrindstoneOps::isCurse);
        return result;
    }

    /**
     * Builds the enchanted book an extraction produces.
     */
    public static ItemStack buildExtractBook(List<IPluginEnchantment> enchantments) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        for (IPluginEnchantment enchantment : enchantments) {
            book = enchantment.addToBook(book);
        }
        return book;
    }

    // ----------------------------------------------------------------------------------------------------
    // Transfer (enchanted item + different item -> enriched item)

    /**
     * Computes the enchantments a transfer moves from {@code source} to {@code target}.
     * <p>
     * Rules: both items are single (amount 1), neither is a book, they are different materials
     * (two items of the same kind are a vanilla repair), and the source material is not restricted.
     * Only {@code minecraft:} enchantments move. An enchantment moves when the target can take it,
     * it conflicts with nothing already on the target or already chosen, and the target does not
     * already hold it at the same or a higher level. Levels above the enchantment's vanilla maximum
     * are lowered unless {@code grindstone.transfer.allow-over-max} is true. {@code KEEP} enchantments
     * stay on the source, a {@code DISABLE} enchantment on the source blocks the whole transfer, curses
     * stay unless {@code grindstone.curses} is true, and at most {@code grindstone.transfer.max-enchantments}
     * (default 16) move at once — highest levels first.
     *
     * @param source the item in grindstone slot 0
     * @param target the item in grindstone slot 1
     * @param player the player (honours {@code disenchantment.bypass.material})
     * @return the moved enchantments with the level they will have on the target, empty when nothing transfers
     */
    public static List<IPluginEnchantment> computeTransfer(ItemStack source, ItemStack target, Player player) {
        if (source == null || target == null) return List.of();
        if (source == target) return List.of();
        if (source.getType().isAir() || target.getType().isAir()) return List.of();
        if (isBook(source.getType()) || isBook(target.getType())) return List.of();

        if (source.getAmount() != 1 || target.getAmount() != 1) {
            DiagnosticUtils.debug("GRINDSTONE", "Transfer: rejected — source or target is a stack");
            return List.of();
        }

        // Same material is a vanilla repair/combine; never touched.
        if (source.getType() == target.getType()) return List.of();

        if (!(player != null && PermissionType.BYPASS_MATERIAL.hasPermission(player))
                && Config.Disenchantment.isMaterialRestricted(source.getType())) {
            DiagnosticUtils.debug("GRINDSTONE", () -> "Transfer: rejected — material " + source.getType() + " is disabled by config");
            return List.of();
        }

        Map<Enchantment, Integer> sourceEnchants = source.getEnchantments();
        if (sourceEnchants.isEmpty()) return List.of();

        Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();
        for (Enchantment enchantment : sourceEnchants.keySet()) {
            if (EventUtils.getStateForKey(states, enchantment.getKey().getKey()) == EnchantmentStateType.DISABLE) {
                DiagnosticUtils.debug("GRINDSTONE", () -> "Transfer: rejected — " + enchantment.getKey() + " has state=DISABLE");
                return List.of();
            }
        }

        // Highest level first so the cap keeps the most valuable enchantments; key as tie-breaker for a stable order.
        List<Map.Entry<Enchantment, Integer>> ordered = new ArrayList<>(sourceEnchants.entrySet());
        ordered.sort(Comparator.<Map.Entry<Enchantment, Integer>>comparingInt(Map.Entry::getValue).reversed()
                .thenComparing(entry -> entry.getKey().getKey().toString()));

        Map<Enchantment, Integer> targetEnchants = target.getEnchantments();
        boolean allowOverMax = Config.Grindstone.Transfer.isAllowOverMax();
        boolean curses = Config.Grindstone.isCursesEnabled();
        int cap = Config.Grindstone.Transfer.getMaxEnchantments();

        List<Enchantment> chosen = new ArrayList<>();
        List<IPluginEnchantment> result = new ArrayList<>();

        for (Map.Entry<Enchantment, Integer> entry : ordered) {
            if (result.size() >= cap) break;

            Enchantment enchantment = entry.getKey();
            if (!"minecraft".equals(enchantment.getKey().getNamespace())) continue;
            if (EventUtils.getStateForKey(states, enchantment.getKey().getKey()) == EnchantmentStateType.KEEP) continue;
            if (!curses && isCurseKey(enchantment.getKey().getKey())) continue;
            if (entry.getValue() <= 0) continue;
            if (!enchantment.canEnchantItem(target)) continue;

            int level = allowOverMax ? entry.getValue() : Math.min(entry.getValue(), enchantment.getMaxLevel());
            if (level <= 0) continue;

            Integer existing = targetEnchants.get(enchantment);
            if (existing != null && existing >= level) continue;

            if (conflictsWithAny(enchantment, targetEnchants, chosen)) continue;

            chosen.add(enchantment);
            result.add(EnchantmentUtils.remapEnchantment(enchantment, level));
        }

        return result;
    }

    private static boolean conflictsWithAny(Enchantment enchantment, Map<Enchantment, Integer> onTarget, List<Enchantment> chosen) {
        for (Enchantment other : onTarget.keySet()) {
            // The same enchantment on the target is an upgrade, not a conflict.
            if (other.equals(enchantment)) continue;
            if (enchantment.conflictsWith(other)) return true;
        }
        for (Enchantment other : chosen) {
            if (enchantment.conflictsWith(other)) return true;
        }
        return false;
    }

    private static boolean isBook(Material material) {
        return material == Material.BOOK || material == Material.ENCHANTED_BOOK || material == Material.WRITTEN_BOOK || material == Material.WRITABLE_BOOK;
    }

    /**
     * Returns a copy of {@code target} carrying the transferred enchantments.
     */
    public static ItemStack buildTransferResult(ItemStack target, List<IPluginEnchantment> transfers) {
        ItemStack result = target.clone();
        for (IPluginEnchantment enchantment : transfers) {
            result = enchantment.addToItem(result);
        }
        return result;
    }

    /**
     * Returns a copy of {@code source} without the given enchantments.
     */
    public static ItemStack stripFromSource(ItemStack source, List<IPluginEnchantment> transfers) {
        ItemStack result = source.clone();
        for (IPluginEnchantment enchantment : transfers) {
            result = enchantment.removeFromItem(result);
        }
        return result;
    }
}
