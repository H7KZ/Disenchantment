package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.SupportedPluginManager;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.AnvilCostUtils;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShatterClickEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
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

        Map<Enchantment, Integer> enchantments = EventUtils.Shatterment.getDisenchantedEnchantments(firstItem, secondItem);

        if (enchantments.isEmpty()) return;

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        if (AnvilCostUtils.getRepairCost(anvilInventory, e.getView()) > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (!PermissionGroupType.SHATTER_EVENT.hasPermission(p)) return;

        int exp = p.getLevel() - AnvilCostUtils.getRepairCost(anvilInventory, e.getView());

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        if (firstItem == null) return;
        ItemStack item = firstItem.clone();
        Map<Enchantment, Integer> enchantmentsToDelete = EventUtils.Shatterment.findEnchantmentsToDelete(enchantments);

        List<ISupportedPlugin> activatedPlugins = SupportedPluginManager.getAllActivatedPlugins();

        EnchantmentStorageMeta resultItemMeta = (EnchantmentStorageMeta) result.getItemMeta();

        if (activatedPlugins.isEmpty()) {
            if (resultItemMeta == null) return;
            item = VanillaPlugin.removeEnchantments(item, resultItemMeta.getStoredEnchants());
            item = VanillaPlugin.removeEnchantments(item, enchantmentsToDelete);
        } else {
            HashMap<Enchantment, Integer> pluginEnchantments = EnchantmentUtils.getItemEnchantments(result);

            for (ISupportedPlugin plugin : activatedPlugins) {
                item = plugin.removeEnchantmentsFromItem(item, pluginEnchantments);
                item = plugin.removeEnchantmentsFromItem(item, enchantmentsToDelete);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        if (Config.Shatterment.Anvil.Repair.isResetEnabled()) AnvilCostUtils.setItemRepairCost(item, 0);

        anvilInventory.setItem(0, item);

        if (secondItem == null) return;

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            anvilInventory.setItem(1, null);
        }

        if (Config.Shatterment.Anvil.Sound.isEnabled())
            p.playSound(
                    p.getLocation(),
                    Sound.BLOCK_ANVIL_USE,
                    Float.parseFloat(Config.Shatterment.Anvil.Sound.getVolume().toString()),
                    Float.parseFloat(Config.Shatterment.Anvil.Sound.getPitch().toString())
            );

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        p.setItemOnCursor(result);
    }
}
