package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EconomyUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import com.jankominek.disenchantment.utils.PreviewLore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@link PrepareAnvilEvent} for disenchantment operations.
 * When a player places an enchanted item in an anvil with a book, this handler
 * prepares an enchanted book result containing the extracted enchantments and
 * sets the appropriate anvil repair cost.
 */
public class DisenchantEvent {
    /**
     * Entry point for the disenchant prepare-anvil event.
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
            return Config.Disenchantment.Economy.isEnabled();
        }

        @Override
        public double getCost() {
            return Config.Disenchantment.Economy.getCost();
        }

        @Override
        public boolean isChargeMessageEnabled() {
            return Config.Disenchantment.Economy.isChargeMessageEnabled();
        }

        @Override
        public boolean isShowCostEnabled() {
            return Config.Disenchantment.Economy.isShowCostEnabled();
        }
    };

    private static void handler(Event event) {
        if (!(event instanceof PrepareAnvilEvent e)) return;

        // Use getViewers() instead of getView().getPlayer() to avoid IncompatibleClassChangeError
        // in environments without Paper's classloader shim (InventoryView: class→interface in 1.21).
        if (e.getInventory().getViewers().isEmpty() || !(e.getInventory().getViewers().get(0) instanceof Player p))
            return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled()) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.isWorldRestricted(p.getWorld())))
            return;

        // Not a special anvil: leave the event untouched so vanilla behaves as without the plugin.
        // The collection below only runs when a hint may be due (it needs to know it is a genuine operation).
        boolean anvilAllowed = AnvilEventGuards.isAnvilAllowed(e.getInventory(), p);
        if (!anvilAllowed && !Config.SpecialAnvils.isHintMessageEnabled()) return;

        DiagnosticUtils.debug("DISENCHANT", () -> "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("DISENCHANT", () -> "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        List<IPluginEnchantment> pluginEnchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, true,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                EventUtils.Disenchantment::getDisenchantedEnchantments,
                p.getWorld(), p);

        if (pluginEnchantments.isEmpty()) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: no eligible enchantments → exit");
            return;
        }

        if (!anvilAllowed) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: not a special anvil → exit");
            AnvilEventGuards.sendSpecialAnvilHint(p);
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: enchantments=[" + names + "] (" + pluginEnchantments.size() + " total)");
        }

        // Only a genuine plugin operation is blocked by maintenance mode — vanilla anvil use
        // (repair/rename/combine) must keep working. No message here: PrepareAnvilEvent fires on
        // every keystroke, the click handler informs the player.
        if (AnvilEventGuards.isMaintenanceActiveFor(p)) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: maintenance active → result cleared");
            e.setResult(null);
            return;
        }

        if (AnvilEventGuards.isOnCooldownQuiet(p)) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: player on cooldown → result cleared (no message, the click path informs)");
            e.setResult(null);
            return;
        }

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: permission denied → exit");
            return;
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        for (IPluginEnchantment pluginEnchantment : pluginEnchantments) {
            book = pluginEnchantment.addToBook(book);
        }

        boolean anyChanceBelowOne = pluginEnchantments.stream()
                .anyMatch(ench -> Config.Disenchantment.getEnchantmentChance(ench.getKey()) < 1.0);

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                pluginEnchantments, Config.Disenchantment.Economy.getCost(), Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts());

        // Everything appended here is marked as preview lore and stripped again when the player takes the
        // book (DisenchantClickEvent) — the chance warning must not stay on the book permanently.
        List<String> previewLines = PreviewLore.previewLines(p, pluginEnchantments, AnvilEventType.DISENCHANTMENT,
                Config.Disenchantment.Economy.isEnabled(), Config.Disenchantment.Economy.isShowCostEnabled(), economyCost);
        if (anyChanceBelowOne) previewLines.add(I18n.Messages.someEnchantmentsMayNotTransfer());
        book = PreviewLore.append(book, previewLines);

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        DiagnosticUtils.debug("DISENCHANT", () -> "PrepareAnvil: anvil cost=" + AnvilCostUtils.countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT, p));
        AnvilEventGuards.applyAnvilCostAndSchedule(e, p, pluginEnchantments, AnvilEventType.DISENCHANTMENT);

        DiagnosticUtils.debug("DISENCHANT", () -> "PrepareAnvil: economy display — enabled=" + Config.Disenchantment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Disenchantment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        AnvilEventGuards.showEconomyActionBarCost(p, ECONOMY_CONFIG, economyCost);
    }
}
