package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@link InventoryClickEvent} when a player clicks the result slot
 * of an anvil during a disenchantment operation. Removes the extracted enchantments
 * from the source item, deducts experience, plays a sound effect, and delivers
 * the enchanted book to the player's cursor.
 */
public class DisenchantClickEvent {
    /**
     * Entry point for the disenchant inventory-click event.
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
    };

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        if (!AnvilEventGuards.isAnvilResultSlotClick(e, p)) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        DiagnosticUtils.debug("DISENCHANT", "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, false,
                (f, s, ip) -> EventUtils.Disenchantment.getDisenchantedEnchantments(f, s, ip),
                (f, s, ip, plugin, world) -> EventUtils.Disenchantment.getDisenchantedEnchantments(f, s, ip, plugin, world),
                p.getWorld());

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("DISENCHANT", "Click: no eligible enchantments → exit");
            return;
        }

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("DISENCHANT", "Click: enchantments=[" + names + "]");
        }

        int repairCost = AnvilCostUtils.getRepairCost(anvilInventory, e.getView());
        DiagnosticUtils.debug("DISENCHANT", "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (!AnvilEventGuards.hasEnoughXp(p, repairCost)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: insufficient XP → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("DISENCHANT", "Click: permission denied → exit");
            return;
        }

        // Economy check
        DiagnosticUtils.debug("DISENCHANT", "Click: economy check — enabled=" + Config.Disenchantment.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomy(p, ECONOMY_CONFIG);
        if (economyResult == AnvilEventGuards.EconomyResult.NOT_AVAILABLE) {
            DiagnosticUtils.debug("DISENCHANT", "Click: economy not available → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyNotAvailable());
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.INSUFFICIENT_FUNDS) {
            DiagnosticUtils.debug("DISENCHANT", "Click: insufficient funds → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyInsufficientFunds(EconomyUtils.format(Config.Disenchantment.Economy.getCost())));
            e.setCancelled(true);
            return;
        }

        PreDisenchantEvent preEvent = new PreDisenchantEvent(p, firstItem.clone(), new ArrayList<>(enchantments));
        org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        int exp = p.getLevel() - repairCost;
        DiagnosticUtils.debug("DISENCHANT", "Click: xp → " + p.getLevel() + " - " + repairCost + " = " + exp);

        // ----------------------------------------------------------------------------------------------------
        // Supported plugins

        ItemStack finalFirstItem = firstItem.clone();
        List<IPluginEnchantment> enchantmentsToDelete = EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments);

        EnchantmentStorageMeta resultItemMeta = (EnchantmentStorageMeta) result.getItemMeta();

        if (activatedPlugins.isEmpty()) {
            if (resultItemMeta == null) return;

            finalFirstItem = EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants());

            for (IPluginEnchantment enchantment : enchantmentsToDelete) {
                finalFirstItem = enchantment.removeFromItem(finalFirstItem);
            }
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                List<IPluginEnchantment> pluginEnchantments = activatedPlugin.getItemEnchantments(result, p.getWorld());

                for (IPluginEnchantment enchantment : pluginEnchantments) {
                    finalFirstItem = enchantment.removeFromItem(finalFirstItem);
                }
            }

            for (IPluginEnchantment enchantment : enchantmentsToDelete) {
                finalFirstItem = enchantment.removeFromItem(finalFirstItem);
            }
        }

        // Supported plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Disenchantment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        anvilInventory.setItem(0, finalFirstItem);

        if (secondItem == null) return;
        AnvilEventGuards.scheduleSecondItemRemoval(p, anvilInventory, secondItem);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        p.setItemOnCursor(result);
        org.bukkit.Bukkit.getPluginManager().callEvent(new PostDisenchantEvent(p, result.clone(), finalFirstItem.clone()));

        if (Config.Disenchantment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Config.Disenchantment.Anvil.Sound.getVolume().floatValue(),
                    Config.Disenchantment.Anvil.Sound.getPitch().floatValue()
            );

        DiagnosticUtils.debug("DISENCHANT", "Click: complete ✓");
    }
}
