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

        if (AnvilEventGuards.isMaintenanceBlocked(p)) {
            e.setResult(null);
            return;
        }

        if (AnvilEventGuards.isWorldBlocked(p, Config.Disenchantment.getDisabledWorlds().contains(p.getWorld())))
            return;

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
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

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: enchantments=[" + names + "] (" + pluginEnchantments.size() + " total)");
        }

        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: player on cooldown → CANCELLED");
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

        if (anyChanceBelowOne) {
            var meta = book.getItemMeta();
            if (meta != null) {
                java.util.List<String> lore = meta.hasLore() && meta.getLore() != null ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<String>();
                lore.add(I18n.Messages.someEnchantmentsMayNotTransfer());
                meta.setLore(lore);
                book.setItemMeta(meta);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: anvil cost=" + AnvilCostUtils.countAnvilCost(pluginEnchantments, AnvilEventType.DISENCHANTMENT, p));
        AnvilEventGuards.applyAnvilCostAndSchedule(e, p, pluginEnchantments, AnvilEventType.DISENCHANTMENT);

        DiagnosticUtils.debug("DISENCHANT", "PrepareAnvil: economy display — enabled=" + Config.Disenchantment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Disenchantment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                pluginEnchantments, Config.Disenchantment.Economy.getCost(), Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts());

        AnvilEventGuards.showEconomyActionBarCost(p, ECONOMY_CONFIG, economyCost);
    }
}
