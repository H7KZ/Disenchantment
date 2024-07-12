package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.events.advanced.DisenchantmentAdvancedClickEvent;
import cz.kominekjan.disenchantment.events.eco.DisenchantmentEcoClickEvent;
import cz.kominekjan.disenchantment.events.excellent.DisenchantmentExcellentClickEvent;
import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.Disenchantment.logger;
import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantment;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!p.hasPermission("disenchantment.anvil")) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld().getName())) return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        if (e.getSlot() != 2) return;

        if (anvilInventory.getItem(2) == null) return;

        ItemStack result = anvilInventory.getItem(2);

        assert result != null;
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
                            firstItem.getEnchantments().keySet() + " in " +
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

        if (Bukkit.getServer().getPluginManager().getPlugin("ExcellentEnchants") != null) {
            DisenchantmentExcellentClickEvent.onDisenchantmentClickEvent(e);
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("EcoEnchants") != null && Bukkit.getServer().getPluginManager().getPlugin("eco") != null) {
            DisenchantmentEcoClickEvent.onDisenchantmentClickEvent(e);
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            DisenchantmentAdvancedClickEvent.onDisenchantmentClickEvent(e);
            return;
        }

        DisenchantmentNormalClickEvent.onDisenchantmentClickEvent(e);
    }
}
