package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
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

    private static void handler(Event event) {
        if (!(event instanceof InventoryClickEvent e)) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        if (e.getSlot() != 2) return;

        // We do not want to continue if the player has an item in cursor as it would delete it.
        if (!p.getItemOnCursor().getType().isAir()) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                enchantments.addAll(EventUtils.Shatterment.getShattermentEnchantments(firstItem, secondItem, false, activatedPlugin, p.getWorld()));
            }
        }

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

        int repairCost = AnvilCostUtils.getRepairCost(anvilInventory, e.getView());
        DiagnosticUtils.debug("SHATTER", "Click: xp check — repairCost=" + repairCost + ", playerLevel=" + p.getLevel());
        if (repairCost > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            DiagnosticUtils.debug("SHATTER", "Click: insufficient XP → CANCELLED");
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) {
            DiagnosticUtils.debug("SHATTER", "Click: permission denied → exit");
            return;
        }

        // Economy check
        DiagnosticUtils.debug("SHATTER", "Click: economy check — enabled=" + Config.Shatterment.Economy.isEnabled() + ", gameMode=" + p.getGameMode());
        if (Config.Shatterment.Economy.isEnabled() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            if (!EconomyUtils.isAvailable()) {
                DiagnosticUtils.debug("SHATTER", "Click: economy not available → CANCELLED");
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyNotAvailable());
                e.setCancelled(true);
                return;
            }
            double economyCost = Config.Shatterment.Economy.getCost();
            if (!EconomyUtils.has(p, economyCost)) {
                DiagnosticUtils.debug("SHATTER", "Click: insufficient funds → CANCELLED");
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyInsufficientFunds(EconomyUtils.format(economyCost)));
                e.setCancelled(true);
                return;
            }
            EconomyUtils.withdraw(p, economyCost);
            if (Config.Shatterment.Economy.isChargeMessageEnabled()) {
                p.sendMessage(I18n.getPrefix() + " " + I18n.Messages.economyCharged(EconomyUtils.format(economyCost)));
            }
        }

        int exp = p.getLevel() - repairCost;
        DiagnosticUtils.debug("SHATTER", "Click: xp → " + p.getLevel() + " - " + repairCost + " = " + exp);

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack finalFirstItem = firstItem.clone();
        List<IPluginEnchantment> enchantmentsToDelete = EventUtils.Shatterment.findEnchantmentsToDelete(enchantments);

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

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Shatterment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        anvilInventory.setItem(0, finalFirstItem);

        if (secondItem == null) return;
        ItemStack finalSecondItem = secondItem.clone();

        // Schedule task to run 2 ticks after the event
        // It is because of EnchantsSquared (they replace second slot to null after 1 tick)
        // Maybe here it is not needed, but it is better to be safe than sorry
        SchedulerUtils.runForEntityLater(Disenchantment.plugin, p, () -> {
            if (finalSecondItem.getAmount() > 1) {
                finalSecondItem.setAmount(finalSecondItem.getAmount() - 1);
                anvilInventory.setItem(1, finalSecondItem);
            } else {
                anvilInventory.setItem(1, null);
            }
        }, 2L);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        p.setItemOnCursor(result);

        if (Config.Shatterment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Float.parseFloat(Config.Shatterment.Anvil.Sound.getVolume().toString()),
                    Float.parseFloat(Config.Shatterment.Anvil.Sound.getPitch().toString())
            );

        DiagnosticUtils.debug("SHATTER", "Click: complete ✓");
    }
}
