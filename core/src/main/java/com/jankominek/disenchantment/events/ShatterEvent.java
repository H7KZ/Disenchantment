package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: player=" + p.getName() + ", world=" + p.getWorld().getName());

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: slot0=" + (firstItem != null ? firstItem.getType() : "null")
                + ", slot1=" + (secondItem != null ? secondItem.getType() : "null"));

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, true,
                EventUtils.Shatterment::getShattermentEnchantments,
                EventUtils.Shatterment::getShattermentEnchantments,
                p.getWorld());

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: no eligible enchantments → exit");
            return;
        }

        if (AnvilEventGuards.isOnCooldown(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: player on cooldown → CANCELLED");
            e.setResult(null);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: permission denied → exit");
            return;
        }

        List<IPluginEnchantment> pluginEnchantments = new ArrayList<>();

        List<IPluginEnchantment> shuffleEnchantments = new ArrayList<>(enchantments);
        Collections.shuffle(shuffleEnchantments);

        int splitCount = Config.Shatterment.getSplitCount();
        int halfSize = Math.min(Math.max(1, splitCount), shuffleEnchantments.size() - 1);

        for (int i = 0; i < halfSize; i++) {
            IPluginEnchantment enchantment = shuffleEnchantments.get(i);
            pluginEnchantments.add(enchantment);
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String allNames = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            String splitNames = pluginEnchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: source enchantments=[" + allNames + "] (" + enchantments.size() + " total)");
            DiagnosticUtils.debug("SHATTER", "PrepareAnvil: splitting → selected=[" + splitNames + "] (halfSize=" + halfSize + ")");
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

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: anvil cost=" + AnvilCostUtils.countAnvilCost(pluginEnchantments, AnvilEventType.SHATTERMENT, p));
        AnvilEventGuards.applyAnvilCostAndSchedule(e, p, pluginEnchantments, AnvilEventType.SHATTERMENT);

        DiagnosticUtils.debug("SHATTER", "PrepareAnvil: economy display — enabled=" + Config.Shatterment.Economy.isEnabled()
                + ", available=" + EconomyUtils.isAvailable()
                + ", show-cost=" + Config.Shatterment.Economy.isShowCostEnabled()
                + ", gameMode=" + p.getGameMode());

        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                pluginEnchantments, Config.Shatterment.Economy.getCost(), Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts());

        AnvilEventGuards.showEconomyActionBarCost(p, ECONOMY_CONFIG, economyCost);
    }
}
