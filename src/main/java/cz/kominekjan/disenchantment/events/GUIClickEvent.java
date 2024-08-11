package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.guis.impl.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;

public class GUIClickEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        if (!(event.getWhoClicked().hasPermission("disenchantment.all") || event.getWhoClicked().hasPermission("disenchantment.gui")))
            return;

        InventoryHolder clickedHolder = event.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            ((NavigationGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof WorldsGUI) {
            if (!event.getWhoClicked().hasPermission("disenchantment.gui.toggle")) {
                event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                return;
            }

            ((WorldsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof RepairGUI) {
            if (!event.getWhoClicked().hasPermission("disenchantment.gui.toggle")) {
                event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                return;
            }

            ((RepairGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            if (!event.getWhoClicked().hasPermission("disenchantment.gui.toggle")) {
                event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                return;
            }

            ((EnchantmentsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof MaterialsGUI) {
            if (!event.getWhoClicked().hasPermission("disenchantment.gui.toggle")) {
                event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                return;
            }

            ((MaterialsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof SoundGUI) {
            if (!event.getWhoClicked().hasPermission("disenchantment.gui.toggle")) {
                event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                return;
            }

            ((SoundGUI) clickedHolder).onInventoryClick(event);
        }
    }
}
