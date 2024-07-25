package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.guis.impl.EnchantmentsGUI;
import cz.kominekjan.disenchantment.guis.impl.NavigationGUI;
import cz.kominekjan.disenchantment.guis.impl.WorldsGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class DisenchantmentGUIClickEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        InventoryHolder clickedHolder = event.getClickedInventory().getHolder();

        if (clickedHolder instanceof NavigationGUI) {
            ((NavigationGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof WorldsGUI) {
            ((WorldsGUI) clickedHolder).onInventoryClick(event);
        } else if (clickedHolder instanceof EnchantmentsGUI) {
            ((EnchantmentsGUI) clickedHolder).onInventoryClick(event);
        }
    }
}
