package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.impl.*;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIClickEvent implements Listener {
    private static void handleEvent(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        InventoryHolder clickedHolder = e.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            if (!PermissionGroupType.GUI.hasPermission(p)) return;

            ((NavigationGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentRepairGUI) {
            if (!PermissionGroupType.GUI_DISENCHANT_REPAIR.hasPermission(p)) return;

            ((DisenchantmentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentRepairGUI) {
            if (!PermissionGroupType.GUI_SHATTER_REPAIR.hasPermission(p)) return;

            ((ShattermentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentSoundGUI) {
            if (!PermissionGroupType.GUI_DISENCHANT_SOUND.hasPermission(p)) return;

            ((DisenchantmentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentSoundGUI) {
            if (!PermissionGroupType.GUI_SHATTER_SOUND.hasPermission(p)) return;

            ((ShattermentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!PermissionGroupType.GUI_WORLDS.hasPermission(p)) return;

            ((WorldsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof MaterialsGUI) {
            if (!PermissionGroupType.GUI_MATERIALS.hasPermission(p)) return;

            ((MaterialsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!PermissionGroupType.GUI_ENCHANTMENTS.hasPermission(p)) return;

            ((EnchantmentsGUI) clickedHolder).onInventoryClick(e);
        }
    }
    @EventHandler (priority = EventPriority.LOWEST)
    public void onInventoryClickLowest(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.LOWEST) handleEvent(e);
    }
    @EventHandler (priority = EventPriority.LOW)
    public void onInventoryClickLow(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.LOW) handleEvent(e);
    }
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryClickNormal(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.NORMAL) handleEvent(e);
    }
    @EventHandler (priority = EventPriority.HIGH)
    public void onInventoryClickHigh(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.HIGH) handleEvent(e);
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInventoryClickHighest(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.HIGHEST) handleEvent(e);
    }
    @EventHandler (priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e) {
        if (Config.getGuiClickEventPriority() == EventPriority.MONITOR) handleEvent(e);
    }
}
