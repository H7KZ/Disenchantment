package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.Disenchantment.logger;
import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantItem;

public class ItemClickEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld())) return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil") || p.hasPermission("disenchantment.anvil.item")))
            return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        if (e.getSlot() != 2) return;

        // We do not want to continue if the player has an item in cursor as it would delete it.
        if(!p.getItemOnCursor().getType().isAir()) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        // do not use "anvilInventory.getResult();" as not present on current spigot.
        ItemStack result = anvilInventory.getItem(2);

        if (result == null) return;

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        ItemStack firstItem = anvilInventory.getItem(0);
        ItemStack secondItem = anvilInventory.getItem(1);

        if (!isEventValidDisenchantItem(firstItem, secondItem)) return;

        AnvilView anvilView = (AnvilView) e.getView();

        EnchantmentStorageMeta resultItemMeta = (EnchantmentStorageMeta) result.getItemMeta();

        if (anvilView.getRepairCost() > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick().isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        int exp = p.getLevel() - anvilView.getRepairCost();

        if (getEnableLogging()) {
            if (getLoggingLevel().equals(LoggingLevels.INFO) || getLoggingLevel().equals(LoggingLevels.DEBUG))
                logger.info(
                        p.getName() + " has disenchanted " +
                                firstItem.getType().name() + " with " +
                                firstItem.getEnchantments().keySet() + " for " +
                                anvilView.getRepairCost() + "xp" + " in " +
                                p.getWorld().getName() + " at " +
                                p.getLocation().getBlockX() + " " +
                                p.getLocation().getBlockY() + " " +
                                p.getLocation().getBlockZ() + "."
                );

            if (getLoggingLevel().equals(LoggingLevels.DEBUG))
                logger.info(
                        firstItem + " " +
                                anvilInventory.getItem(1) + " " +
                                anvilInventory.getItem(2) + " "
                );
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack item = firstItem.clone();

        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        boolean atLeastOnePluginEnabled = false;

        for (IPlugin plugin : activatedPlugins.values()) {
            item = plugin.removeEnchantments(item, resultItemMeta.getStoredEnchants());
            atLeastOnePluginEnabled = true;
        }

        if (!atLeastOnePluginEnabled) item = VanillaPlugin.removeEnchantments(item, resultItemMeta.getStoredEnchants());

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        if (getEnableRepairReset() && (item.getItemMeta() instanceof Repairable meta)) {
            meta.setRepairCost(0);
            item.setItemMeta(meta);
        }

        anvilInventory.setItem(0, item);

        if (secondItem.getAmount() > 1) {
            secondItem.setAmount(secondItem.getAmount() - 1);
        } else {
            anvilInventory.setItem(1, null);
        }

        if (getEnableAnvilSound())
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, Float.parseFloat(getAnvilSoundVolume().toString()), Float.parseFloat(getAnvilSoundPitch().toString()));

        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) p.setLevel(exp);

        p.setItemOnCursor(result);
    }
}
