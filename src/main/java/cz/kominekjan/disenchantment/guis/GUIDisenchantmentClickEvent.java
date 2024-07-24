package cz.kominekjan.disenchantment.guis;

import cz.kominekjan.disenchantment.guis.impl.NavigationGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIDisenchantmentClickEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        InventoryHolder clickedHolder = event.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            NavigationGUI.onInventoryClick(event);
        }
    }
}
