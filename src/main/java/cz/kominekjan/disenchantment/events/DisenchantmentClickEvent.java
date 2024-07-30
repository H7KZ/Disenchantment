package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.plugins.DisenchantmentPluginManager;
import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
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

import java.util.HashMap;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.Disenchantment.logger;
import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.libs.nbteditor.NBT.setNBTRepairCost;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantment;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil"))) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld())) return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        if (e.getSlot() != 2) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        ItemStack result = anvilInventory.getResult();

        if (result == null) return;

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        ItemStack firstItem = anvilInventory.getItem(0);

        if (!isEventValidDisenchantment(firstItem, anvilInventory.getItem(1))) return;

        if (anvilInventory.getRepairCost() > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick().isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        if (getEnableLogging()) {
            if (getLoggingLevel().equals(LoggingLevels.INFO) || getLoggingLevel().equals(LoggingLevels.DEBUG))
                logger.info(
                        p.getName() + " has disenchanted " +
                                firstItem.getType().name() + " with " +
                                firstItem.getEnchantments().keySet() + " for " +
                                (((AnvilInventory) e.getInventory()).getRepairCost()) + "xp" + " in " +
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

        int exp = p.getLevel() - anvilInventory.getRepairCost();
        ItemStack secondItem = anvilInventory.getItem(1);

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack item = firstItem.clone();

        HashMap<String, IDisenchantmentPlugin> activatedPlugins = DisenchantmentPluginManager.getActivatedPlugins();

        boolean atLeastOnePluginEnabled = false;

        for (IDisenchantmentPlugin plugin : activatedPlugins.values()) {
            item = plugin.removeItemEnchantments(firstItem);
            atLeastOnePluginEnabled = true;
        }

        if (!atLeastOnePluginEnabled) item = VanillaPlugin.removeItemEnchantments(firstItem);

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        if (getEnableRepairReset()) item = setNBTRepairCost(item, 0);

        anvilInventory.setItem(0, item);

        if (secondItem == null) return;

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
