package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.guis.impl.*;
import com.jankominek.disenchantment.types.AnvilFeature;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener that routes inventory click events to the appropriate GUI handler.
 * Checks the clicked inventory holder type and delegates to the matching
 * GUI implementation (navigation, repair, sound, worlds, materials, or enchantments)
 * after verifying the player's permissions.
 *
 * <p>While one of the plugin GUIs is the open (top) inventory, <b>every</b> click and drag in that
 * view is cancelled at this router level first &mdash; this covers shift-clicks from the player's
 * inventory, double-click collect-to-cursor, number-key swaps, clicks on empty slots, drags and
 * permission-denied clicks. Clicks on the GUI inventory itself are then dispatched to the GUI's
 * item handlers.</p>
 */
public class GUIClickEvent implements Listener {
    /**
     * Handles inventory click events for all plugin GUI screens.
     * Routes the event to the correct GUI handler based on the inventory holder type.
     *
     * @param event the inventory click event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(InventoryClickEvent event) {
        try {
            this.handler(event);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    /**
     * Cancels any drag that happens while a plugin GUI is the open (top) inventory.
     * Drags that only touch the player's own inventory are cancelled too, which is strict but
     * harmless: no legitimate interaction with these GUIs involves dragging.
     *
     * @param event the inventory drag event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(InventoryDragEvent event) {
        try {
            // getInventory() is the top inventory; avoids InventoryView (class vs. interface across MC versions)
            if (isPluginGui(event.getInventory().getHolder())) event.setCancelled(true);
        } catch (Exception e) {
            DiagnosticUtils.throwReport(e);
        }
    }

    private static boolean isPluginGui(InventoryHolder holder) {
        return holder instanceof NavigationGUI
                || holder instanceof RepairGUI
                || holder instanceof SoundGUI
                || holder instanceof WorldsGUI
                || holder instanceof MaterialsGUI
                || holder instanceof EnchantmentsGUI
                || holder instanceof EconomyGUI
                || holder instanceof SplitCountGUI
                || holder instanceof StatsGUI
                || holder instanceof BedrockAnvilGUI;
    }

    private void handler(InventoryClickEvent e) {
        // Cancel first, for every click (top or bottom inventory, any click type) while our GUI is open.
        // Individual GUI item handlers may only rely on this; none re-enable the event.
        if (isPluginGui(e.getInventory().getHolder())) e.setCancelled(true);

        if (e.getClickedInventory() == null) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        // The Bedrock anvil menu works with clicks in the player's own inventory too (tapping an item puts it in),
        // so it gets the event whichever half of the window was clicked.
        if (e.getInventory().getHolder() instanceof BedrockAnvilGUI bedrockAnvilGUI) {
            bedrockAnvilGUI.onInventoryClick(e);
            return;
        }

        InventoryHolder clickedHolder = e.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            if (!PermissionGroupType.GUI.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for NavigationGUI");
                return;
            }
            ((NavigationGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof RepairGUI repairGUI) {
            PermissionGroupType permission = repairGUI.getFeature() == AnvilFeature.DISENCHANTMENT
                    ? PermissionGroupType.GUI_DISENCHANT_REPAIR
                    : PermissionGroupType.GUI_SHATTER_REPAIR;
            if (!permission.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for RepairGUI (" + repairGUI.getFeature() + ")");
                return;
            }
            repairGUI.onInventoryClick(e);
        } else if (clickedHolder instanceof SoundGUI soundGUI) {
            PermissionGroupType permission = soundGUI.getFeature() == AnvilFeature.DISENCHANTMENT
                    ? PermissionGroupType.GUI_DISENCHANT_SOUND
                    : PermissionGroupType.GUI_SHATTER_SOUND;
            if (!permission.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for SoundGUI (" + soundGUI.getFeature() + ")");
                return;
            }
            soundGUI.onInventoryClick(e);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!PermissionGroupType.GUI_WORLDS.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for WorldsGUI");
                return;
            }
            ((WorldsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof MaterialsGUI) {
            if (!PermissionGroupType.GUI_MATERIALS.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for MaterialsGUI");
                return;
            }
            ((MaterialsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!PermissionGroupType.GUI_ENCHANTMENTS.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for EnchantmentsGUI");
                return;
            }
            ((EnchantmentsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof EconomyGUI economyGUI) {
            PermissionGroupType permission = economyGUI.getFeature() == AnvilFeature.DISENCHANTMENT
                    ? PermissionGroupType.GUI_DISENCHANT_ECONOMY
                    : PermissionGroupType.GUI_SHATTER_ECONOMY;
            if (!permission.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for EconomyGUI (" + economyGUI.getFeature() + ")");
                return;
            }
            economyGUI.onInventoryClick(e);
        } else if (clickedHolder instanceof SplitCountGUI splitCountGUI) {
            if (!PermissionGroupType.GUI_SHATTER_REPAIR.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for SplitCountGUI");
                return;
            }
            splitCountGUI.onInventoryClick(e);
        } else if (clickedHolder instanceof StatsGUI statsGUI) {
            if (!PermissionGroupType.GUI.hasPermission(p)) {
                DiagnosticUtils.debug("GUI", "Click denied: " + p.getName() + " lacks permission for StatsGUI");
                return;
            }
            statsGUI.onInventoryClick(e);
        }
    }
}
