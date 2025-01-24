package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.guis.impl.*;
import com.jankominek.disenchantment.types.PermissionGroupType;
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
}
