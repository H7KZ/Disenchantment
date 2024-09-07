package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.guis.impl.*;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
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

        if (!PermissionGoal.GUI_USE.checkPermission(p))
            return;

        InventoryHolder clickedHolder = e.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            ((NavigationGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentRepairGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_REPAIR.checkPermission(p, true)) return;

            ((DisenchantmentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentRepairGUI) {
            if (!PermissionGoal.GUI_EDIT_SHATTERMENT_REPAIR.checkPermission(p, true)) return;

            ((ShattermentRepairGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantmentSoundGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_SOUND.checkPermission(p, true)) return;

            ((DisenchantmentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof ShattermentSoundGUI) {
            if (!PermissionGoal.GUI_EDIT_SHATTERMENT_SOUND.checkPermission(p, true)) return;

            ((ShattermentSoundGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!PermissionGoal.GUI_EDIT_ALLOWED_WORLDS.checkPermission(p, true)) return;

            ((WorldsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof DisenchantMaterialsGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_MATERIALS.checkPermission(p, true)) return;

            ((DisenchantMaterialsGUI) clickedHolder).onInventoryClick(e);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!PermissionGoal.GUI_EDIT_ENCHANTMENT_STATES.checkPermission(p, true)) return;

            ((EnchantmentsGUI) clickedHolder).onInventoryClick(e);
        }
    }
}
