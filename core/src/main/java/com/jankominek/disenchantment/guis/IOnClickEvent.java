package com.jankominek.disenchantment.guis;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Functional interface for handling inventory click events on GUI items.
 */
public interface IOnClickEvent {
    /**
     * Called when a GUI item is clicked.
     *
     * @param event the inventory click event
     */
    void onClick(InventoryClickEvent event);
}
