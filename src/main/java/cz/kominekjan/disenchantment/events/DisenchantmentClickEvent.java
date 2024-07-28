package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.events.advanced.DisenchantmentAdvancedClickEvent;
import cz.kominekjan.disenchantment.events.eco.DisenchantmentEcoClickEvent;
import cz.kominekjan.disenchantment.events.excellent.DisenchantmentExcellentClickEvent;
import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalClickEvent;
import cz.kominekjan.disenchantment.events.squared.DisenchantmentSquaredClickEvent;
import cz.kominekjan.disenchantment.events.uber.DisenchantmentUberClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import static cz.kominekjan.disenchantment.Disenchantment.*;
import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.AnvilCostUtil.countAnvilCost;
import static cz.kominekjan.disenchantment.utils.EventCheckUtil.isEventValidDisenchantment;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil"))) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld())) return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        if (e.getSlot() != 2) return;

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

        if (getEnableLogging() && (getLoggingLevel().equals(LoggingLevels.INFO) || getLoggingLevel().equals(LoggingLevels.DEBUG)))
            logger.info(
                    p.getName() + " has disenchanted " +
                            firstItem.getType().name() + " with " +
                            firstItem.getEnchantments().keySet() + " for " +
                            countAnvilCost(result.getEnchantments()) + "xp" + " in " +
                            p.getWorld().getName() + " at " +
                            p.getLocation().getBlockX() + " " +
                            p.getLocation().getBlockY() + " " +
                            p.getLocation().getBlockZ() + "."
            );

        if (getEnableLogging() && getLoggingLevel().equals(LoggingLevels.DEBUG))
            logger.info(
                    firstItem + " " +
                            anvilInventory.getItem(1) + " " +
                            anvilInventory.getItem(2) + " "
            );

        if (activatedPlugins.contains("ExcellentEnchants"))
            DisenchantmentExcellentClickEvent.onDisenchantmentClickEvent(e);

        else if (activatedPlugins.contains("EcoEnchants"))
            DisenchantmentEcoClickEvent.onDisenchantmentClickEvent(e);

        else if (activatedPlugins.contains("AdvancedEnchantments"))
            DisenchantmentAdvancedClickEvent.onDisenchantmentClickEvent(e);

        else if (activatedPlugins.contains("UberEnchant"))
            DisenchantmentUberClickEvent.onDisenchantmentClickEvent(e);

        else if (activatedPlugins.contains("EnchantsSquared"))
            DisenchantmentSquaredClickEvent.onDisenchantmentClickEvent(e);

        else
            DisenchantmentNormalClickEvent.onDisenchantmentClickEvent(e);
    }
}
