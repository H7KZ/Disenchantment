package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.guis.impl.*;
import com.jankominek.disenchantment.permissions.PermissionGroups;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIClickEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        InventoryHolder clickedHolder = e.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            if (!PermissionGroups.GUI.checkPermission(p)) return;

            ((NavigationGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentRepairGUI) {
            if (!PermissionGroups.GUI_DISENCHANT_REPAIR.checkPermission(p)) return;

            ((DisenchantmentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentRepairGUI) {
            if (!PermissionGroups.GUI_SHATTER_REPAIR.checkPermission(p)) return;

            ((ShattermentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentSoundGUI) {
            if (!PermissionGroups.GUI_DISENCHANT_SOUND.checkPermission(p)) return;

            ((DisenchantmentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentSoundGUI) {
            if (!PermissionGroups.GUI_SHATTER_SOUND.checkPermission(p)) return;

            ((ShattermentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!PermissionGroups.GUI_WORLDS.checkPermission(p)) return;

            ((WorldsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof MaterialsGUI) {
            if (!PermissionGroups.GUI_MATERIALS.checkPermission(p)) return;

            ((MaterialsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!PermissionGroups.GUI_ENCHANTMENTS.checkPermission(p)) return;

            ((EnchantmentsGUI) clickedHolder).onInventoryClick(e);
        }
    }
}
