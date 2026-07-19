package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import com.jankominek.disenchantment.events.api.PreShatterEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the {@link InventoryClickEvent} when a player clicks the result slot
 * of an anvil during a shatterment (book-splitting) operation. Removes the
 * transferred enchantments from the source book, deducts experience, plays a
 * sound effect, and delivers the resulting enchanted book to the player's cursor.
 */
public class ShatterClickEvent {
    /**
     * Entry point for the shatter inventory-click event.
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
    };

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        Player p = AnvilEventGuards.getPlayer(e);
        if (p == null) return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled()) return;

        if (!AnvilEventGuards.isAnvilResultSlotClick(e, p)) return;

        if (AnvilEventGuards.isMaintenanceBlocked(p)) return;

        if (AnvilEventGuards.isWorldBlocked(p, Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))) return;

        if (AnvilEventGuards.isOnCooldown(p)) {
            e.setCancelled(true);
            return;
        }

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (firstItem == null) return;
        if (secondItem == null) return;

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = AnvilEventGuards.collectEnchantments(
                firstItem, secondItem, false,
                EventUtils.Shatterment::getShattermentEnchantments,
                EventUtils.Shatterment::getShattermentEnchantments,
                p.getWorld(), p);

        if (enchantments.isEmpty()) {
            DiagnosticUtils.debug("SHATTER", "Click: no eligible enchantments → exit");
            return;
        }

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        DiagnosticUtils.debug("SHATTER", "Click: player=" + p.getName() + ", result=" + result.getType() + ", gameMode=" + p.getGameMode());

        if (DiagnosticUtils.isDebugEnabled()) {
            String names = enchantments.stream().map(ench -> ench.getKey() + ":" + ench.getLevel()).collect(Collectors.joining(", "));
            DiagnosticUtils.debug("SHATTER", "Click: enchantments=[" + names + "]");
        }

        if (e.isShiftClick()) {
            DiagnosticUtils.debug("SHATTER", "Click: shift-click blocked → CANCELLED");
            e.setCancelled(true);
            return;
        }

        int repairCost = AnvilEventGuards.peekBypassCost(p, AnvilCostUtils.getRepairCost(anvilInventory, e.getView()));
        DiagnosticUtils.debug("SHATTER", "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (!AnvilEventGuards.hasEnoughXp(p, repairCost)) {
            DiagnosticUtils.debug("SHATTER", "Click: insufficient XP → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "Click: permission denied → exit");
            return;
        }

        PreShatterEvent preEvent = new PreShatterEvent(p, firstItem.clone(), new ArrayList<>(enchantments));
        org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        // Economy check — runs after PreShatterEvent so cancellation doesn't charge the player
        DiagnosticUtils.debug("SHATTER", "Click: economy check — enabled=" + Config.Shatterment.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        double economyCost = AnvilCostUtils.economyCostForEnchantments(
                preEvent.getEnchantments(), Config.Shatterment.Economy.getCost(), Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts());
        AnvilEventGuards.EconomyResult economyResult = AnvilEventGuards.processEconomyCost(p, ECONOMY_CONFIG, economyCost);
        if (economyResult == AnvilEventGuards.EconomyResult.NOT_AVAILABLE) {
            DiagnosticUtils.debug("SHATTER", "Click: economy not available → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyNotAvailable());
            e.setCancelled(true);
            return;
        }
        if (economyResult == AnvilEventGuards.EconomyResult.INSUFFICIENT_FUNDS) {
            DiagnosticUtils.debug("SHATTER", "Click: insufficient funds → CANCELLED");
            p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
            e.setCancelled(true);
            return;
        }

        AnvilEventGuards.clearBypassCost(p);

        int exp = p.getLevel() - repairCost;
        DiagnosticUtils.debug("SHATTER", "Click: xp → " + p.getLevel() + " - " + repairCost + " = " + exp);

        // ----------------------------------------------------------------------------------------------------
        // Shatterment plugins

        ItemStack finalFirstItem = firstItem.clone();
        List<IPluginEnchantment> enchantmentsToDelete = EventUtils.Shatterment.findEnchantmentsToDelete(preEvent.getEnchantments());

        EnchantmentStorageMeta resultItemMeta = (EnchantmentStorageMeta) result.getItemMeta();

        if (activatedPlugins.isEmpty()) {
            if (resultItemMeta == null) return;

            finalFirstItem = EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants());

            for (IPluginEnchantment enchantment : enchantmentsToDelete) {
                // Use removeFromBook since firstItem is always an ENCHANTED_BOOK in shatterment
                finalFirstItem = enchantment.removeFromBook(finalFirstItem);
            }
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                List<IPluginEnchantment> pluginEnchantments = activatedPlugin.getItemEnchantments(result, p.getWorld());

                for (IPluginEnchantment enchantment : pluginEnchantments) {
                    // Use removeFromBook since firstItem is always an ENCHANTED_BOOK in shatterment
                    finalFirstItem = enchantment.removeFromBook(finalFirstItem);
                }
            }

            for (IPluginEnchantment enchantment : enchantmentsToDelete) {
                // Use removeFromBook since firstItem is always an ENCHANTED_BOOK in shatterment
                finalFirstItem = enchantment.removeFromBook(finalFirstItem);
            }
        }

        // Shatterment plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Shatterment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        anvilInventory.setItem(0, finalFirstItem);
        AnvilEventGuards.scheduleSecondItemRemoval(p, anvilInventory, secondItem);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        // Per-enchantment chance roll — failed enchantments are destroyed (already stripped from
        // the source book above via the full stored-enchant set) rather than transferred to the result.
        if (resultItemMeta != null) {
            Map<org.bukkit.enchantments.Enchantment, Integer> stored = new HashMap<>(resultItemMeta.getStoredEnchants());
            boolean rollChanged = false;
            for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : stored.entrySet()) {
                double chance = Config.Shatterment.getEnchantmentChance(entry.getKey().getKey().getKey());
                if (chance < 1.0 && Math.random() >= chance) {
                    resultItemMeta.removeStoredEnchant(entry.getKey());
                    rollChanged = true;
                }
            }
            if (rollChanged) result.setItemMeta(resultItemMeta);
        }

        p.setItemOnCursor(result);
        boolean creative = p.getGameMode() == org.bukkit.GameMode.CREATIVE;
        int xpCost = creative ? 0 : repairCost;
        double finalEconomyCost = (Config.Shatterment.Economy.isEnabled() && !creative) ? economyCost : 0.0;
        AnvilEventGuards.recordCooldownOperation(p);
        org.bukkit.Bukkit.getPluginManager().callEvent(new PostShatterEvent(p, result.clone(), finalFirstItem.clone(), xpCost, finalEconomyCost));

        if (Config.Shatterment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Config.Shatterment.Anvil.Sound.getVolume().floatValue(),
                    Config.Shatterment.Anvil.Sound.getPitch().floatValue()
            );

        DiagnosticUtils.debug("SHATTER", "Click: complete ✓");
    }
}
