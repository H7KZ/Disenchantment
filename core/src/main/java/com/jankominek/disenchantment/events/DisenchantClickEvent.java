package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Bukkit;
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

public class DisenchantClickEvent {
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

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        if (e.getSlot() != 2) return;

        // We do not want to continue if the player has an item in cursor as it would delete it.
        if (!p.getItemOnCursor().getType().isAir()) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        List<IPluginEnchantment> enchantments = new ArrayList<>();

        if (activatedPlugins.isEmpty()) {
            enchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, false));
        } else {
            for (ISupportedPlugin activatedPlugin : activatedPlugins) {
                enchantments.addAll(EventUtils.Disenchantment.getDisenchantedEnchantments(firstItem, secondItem, false, activatedPlugin));
            }
        }

        if (enchantments.isEmpty()) return;

        if (AnvilCostUtils.getRepairCost(anvilInventory, e.getView()) > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)) return;

        int exp = p.getLevel() - AnvilCostUtils.getRepairCost(anvilInventory, e.getView());

        // ----------------------------------------------------------------------------------------------------
        // Supported plugins

        if (firstItem == null) return;
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
                List<IPluginEnchantment> pluginEnchantments = activatedPlugin.getItemEnchantments(result);

                for (IPluginEnchantment enchantment : pluginEnchantments) {
                    finalFirstItem = enchantment.removeFromItem(finalFirstItem);
                }

                for (IPluginEnchantment enchantment : enchantmentsToDelete) {
                    finalFirstItem = enchantment.removeFromItem(finalFirstItem);
                }
            }
        }

        // Supported plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Disenchantment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(finalFirstItem, 0);

        anvilInventory.setItem(0, finalFirstItem);

        if (secondItem == null) return;
        ItemStack finalSecondItem = secondItem.clone();

        // Schedule task to run 2 ticks after the event
        // It is because of EnchantsSquared (they replace second slot to null after 1 tick)
        Bukkit.getScheduler().runTaskLater(Disenchantment.plugin, () -> {
            if (finalSecondItem.getAmount() > 1) {
                finalSecondItem.setAmount(finalSecondItem.getAmount() - 1);

                anvilInventory.setItem(1, finalSecondItem);
            } else {
                anvilInventory.setItem(1, null);
            }
        }, 2L);

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        p.setItemOnCursor(result);

        if (Config.Disenchantment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Float.parseFloat(Config.Disenchantment.Anvil.Sound.getVolume().toString()),
                    Float.parseFloat(Config.Disenchantment.Anvil.Sound.getPitch().toString())
            );
    }
}
