package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.types.SplitCountRange;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handles the {@link PrepareAnvilEvent} for shatterment (book-splitting) operations.
 * When a player places an enchanted book in the first anvil slot with a regular book,
 * this handler prepares a result book containing a random subset (half) of the
 * source book's enchantments and sets the appropriate anvil cost.
 */
public class ShatterEvent {
    /**
     * Entry point for the shatter prepare-anvil event.
     * Delegates to the internal handler and reports any exceptions via diagnostics.
     *
     * @param event the Bukkit event to process
     */
    public static void onEvent(Event event) {
        try {
            handler(event);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    private static final AnvilEventGuards.EconomyConfig ECONOMY_CONFIG = new AnvilEventGuards.EconomyConfig() {
        @Override
        public boolean isEnabled() {
            return Config.Shatterment.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Shatterment.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Shatterment.Economy.isChargeMessageEnabled();
        }

        @Override
        public boolean isShowCostEnabled() {
            return Config.Shatterment.Economy.isShowCostEnabled();
        }
    };

    private static void handler(Event event) {
        if (!(event instanceof PrepareAnvilEvent e)) return;

        // Use getViewers() instead of getView().getPlayer() to avoid IncompatibleClassChangeError
        // in environments without Paper's classloader shim (InventoryView: class→interface in 1.21).
        if (e.getInventory().getViewers().isEmpty() || !(e.getInventory().getViewers().get(0) instanceof Player p))
            return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled()) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Shatterment.isWorldRestricted(p.getWorld()))) return;

        DiagnosticUtils.debug("SHATTER", () -> "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("SHATTER", () -> "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        invalidateSelectionIfInputsChanged(p, firstItem, secondItem);

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, true,
                EventUtils.Shatterment::getShattermentEnchantments,
                EventUtils.Shatterment::getShattermentEnchantments,
                p.getWorld(), p);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: no eligible enchantments → exit");
            return;
        }

        // Only a genuine plugin operation is blocked by maintenance mode — vanilla anvil use
        // (repair/rename/combine) must keep working. No message here: PrepareAnvilEvent fires on
        // every keystroke, the click handler informs the player.
        if (AnvilEventGuards.isMaintenanceActiveFor(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: maintenance active → result cleared");
            e.setResult(null);
            return;
        }

        if (AnvilEventGuards.isOnCooldownQuiet(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: player on cooldown → result cleared (no message, the click path informs)");
            e.setResult(null);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: permission denied → exit");
            return;
        }

        // The chosen subset is reused while the inputs stay the same: PrepareAnvilEvent fires again on
        // every rename keystroke, and re-rolling there made the selected enchantments and cost flicker.
        List<IPluginEnchantment> pluginEnchantments = selectSplit(p, enchantments, firstItem, secondItem);

        if (pluginEnchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: nothing can be split off without emptying the source → exit");
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String allNames = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            String splitNames = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: source enchantments=[" + allNames + "] (" + enchantments.size() + " total)");
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: splitting → selected=[" + splitNames + "]");
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment pluginEnchantment : pluginEnchantments) {
            book = pluginEnchantment.addToBook(book);
        }

        boolean anyChanceBelowOne = pluginEnchantments.stream()
                .anyMatch(ench -> Config.Shatterment.getEnchantmentChance(ench.getKey()) < 1.0);

        if (anyChanceBelowOne) {
            var meta = book.getItemMeta();
            if (meta != null) {
                java.util.List<String> lore = meta.hasLore() && meta.getLore() != null ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<String>();
                lore.add(com.jankominek.disenchantment.config.I18n.Messages.someEnchantmentsMayNotTransfer());
                meta.setLore(lore);
                book.setItemMeta(meta);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        DiagnosticUtils.debug("SHATTER", () -> "PrepareAnvil: anvil cost=" + AnvilCostUtils.countAnvilCost(pluginEnchantments, AnvilEventType.SHATTERMENT, p));
        AnvilEventGuards.applyAnvilCostAndSchedule(e, p, pluginEnchantments, AnvilEventType.SHATTERMENT);

        DiagnosticUtils.debug("SHATTER", () -> "PrepareAnvil: economy display — enabled=" + Config.Shatterment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Shatterment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                pluginEnchantments, Config.Shatterment.Economy.getCost(), Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts());

        AnvilEventGuards.showEconomyActionBarCost(p, ECONOMY_CONFIG, economyCost);
    }

    // ----------------------------------------------------------------------------------------------------
    // Stable split selection

    /**
     * The split subset chosen for one set of anvil inputs.
     *
     * @param first      snapshot of slot 0 when the subset was rolled
     * @param second     snapshot of slot 1 when the subset was rolled
     * @param splitRange the configured split count the roll was made with
     * @param keys       keys of the chosen enchantments, in result order
     */
    private record CachedSelection(ItemStack first, ItemStack second, SplitCountRange splitRange, List<String> keys) {
    }

    // ConcurrentHashMap for Folia region-thread safety (same reasoning as the bypass-cost cache).
    private static final Map<UUID, CachedSelection> selectionCache = new ConcurrentHashMap<>();

    private static boolean sameStack(ItemStack a, ItemStack b) {
        boolean aEmpty = a == null || a.getType().isAir();
        boolean bEmpty = b == null || b.getType().isAir();
        if (aEmpty || bEmpty) return aEmpty && bEmpty;
        return a.isSimilar(b) && a.getAmount() == b.getAmount();
    }

    private static void invalidateSelectionIfInputsChanged(Player p, ItemStack first, ItemStack second) {
        CachedSelection cached = selectionCache.get(p.getUniqueId());
        if (cached != null && !(sameStack(cached.first(), first) && sameStack(cached.second(), second))) {
            selectionCache.remove(p.getUniqueId());
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: inputs changed → cached split selection dropped");
        }
    }

    /**
     * Forgets the player's cached split selection. Called when an operation completes (the next
     * one must roll afresh) and when the player closes the anvil or quits.
     *
     * @param p the player
     */
    public static void clearSelection(Player p) {
        selectionCache.remove(p.getUniqueId());
    }

    /**
     * Forgets every cached split selection. Called on reload, since states, chances or the split
     * count may have changed under a cached subset.
     */
    public static void clearAllSelections() {
        selectionCache.clear();
    }

    /**
     * Returns the enchantments to split off for the current inputs: the cached subset while the
     * inputs, the split-count config and the eligible enchantments are unchanged, otherwise a fresh
     * random roll (which is then cached). Empty when nothing can be split without emptying the source.
     */
    private static List<IPluginEnchantment> selectSplit(Player p, List<IPluginEnchantment> enchantments, ItemStack firstItem, ItemStack secondItem) {
        SplitCountRange splitCountRange = Config.Shatterment.getSplitCount();

        CachedSelection cached = selectionCache.get(p.getUniqueId());
        if (cached != null && cached.splitRange().equals(splitCountRange)) {
            Map<String, IPluginEnchantment> byKey = new HashMap<>();
            for (IPluginEnchantment ench : enchantments) byKey.put(ench.getKey(), ench);

            List<IPluginEnchantment> reused = new ArrayList<>();
            for (String key : cached.keys()) {
                IPluginEnchantment ench = byKey.get(key);
                if (ench == null) {
                    reused = null;
                    break;
                }
                reused.add(ench);
            }
            if (reused != null && !reused.isEmpty()) {
                DiagnosticUtils.debug("SHATTER", "PrepareAnvil: reusing cached split selection");
                return reused;
            }
        }

        List<IPluginEnchantment> shuffleEnchantments = new ArrayList<>(enchantments);
        Collections.shuffle(shuffleEnchantments);

        int splitCount = splitCountRange.isFixed()
                ? splitCountRange.min()
                : java.util.concurrent.ThreadLocalRandom.current().nextInt(splitCountRange.min(), splitCountRange.max() + 1);
        // At least one enchantment must stay on the source book. KEEP-state enchantments already
        // do; otherwise reserve one of the splittable ones.
        int maxSplit = EventUtils.Shatterment.hasKeptEnchantment(firstItem)
                ? shuffleEnchantments.size()
                : shuffleEnchantments.size() - 1;
        int halfSize = Math.min(Math.max(1, splitCount), maxSplit);

        if (halfSize < 1) return List.of();

        List<IPluginEnchantment> selected = new ArrayList<>(shuffleEnchantments.subList(0, halfSize));
        selectionCache.put(p.getUniqueId(), new CachedSelection(
                firstItem.clone(), secondItem.clone(), splitCountRange,
                selected.stream().map(IPluginEnchantment::getKey).collect(Collectors.toList())));
        return selected;
    }
}
