package cz.kominekjan.disenchantment.events;

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

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.events.DisenchantmentEvent.isValid;

public class DisenchantmentClickEvent implements Listener {
    @EventHandler
    public void onDisenchantmentClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        if (!p.hasPermission("disenchantment.anvil")) return;

        if (!enabled || config.getStringList("disabled-worlds").contains(p.getWorld().getName())) return;

        if (e.getInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory anvilInventory = (AnvilInventory) e.getInventory();

        if (e.getSlot() != 2) return;

        if (anvilInventory.getItem(2) == null) return;

        ItemStack result = anvilInventory.getItem(2);

        if (result.getType() != Material.ENCHANTED_BOOK) return;

        if (!isValid(anvilInventory.getItem(0), anvilInventory.getItem(1))) return;

        if (anvilInventory.getRepairCost() > p.getLevel() && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (e.getClick().isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("ExcellentEnchants") != null) {
            DisenchantmentExcellentClickEvent.onDisenchantmentClickEvent(e);
        } else if (Bukkit.getServer().getPluginManager().getPlugin("EcoEnchants") != null && Bukkit.getServer().getPluginManager().getPlugin("eco") != null) {
            DisenchantmentEcoClickEvent.onDisenchantmentClickEvent(e);
        } else {
            DisenchantmentNormalClickEvent.onDisenchantmentClickEvent(e);
        }
    }
}
