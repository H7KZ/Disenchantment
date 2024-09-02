package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.guis.impl.*;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIClickEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        HumanEntity human = event.getWhoClicked();

        if (!PermissionGoal.GUI_USE.checkPermission(human))
            return;

        InventoryHolder clickedHolder = event.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            ((NavigationGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof DisenchantmentRepairGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_REPAIR.checkPermission(human, true)) return;

            ((DisenchantmentRepairGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof DisenchantmentSoundGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_SOUND.checkPermission(human, true)) return;

            ((DisenchantmentSoundGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!PermissionGoal.GUI_EDIT_ALLOWED_WORLDS.checkPermission(human, true)) return;

            ((WorldsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof DisenchantMaterialsGUI) {
            if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_MATERIALS.checkPermission(human, true)) return;

            ((DisenchantMaterialsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!PermissionGoal.GUI_EDIT_ENCHANTMENT_STATES.checkPermission(human, true)) return;

            ((EnchantmentsGUI) clickedHolder).onInventoryClick(event);
        }
    }
}
