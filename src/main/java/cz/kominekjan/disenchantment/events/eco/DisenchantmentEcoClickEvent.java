package cz.kominekjan.disenchantment.events.eco;

import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalClickEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DisenchantmentEcoClickEvent {
    public static void onDisenchantmentClickEvent(InventoryClickEvent e) {
        DisenchantmentNormalClickEvent.onDisenchantmentClickEvent(e);
    }
}
