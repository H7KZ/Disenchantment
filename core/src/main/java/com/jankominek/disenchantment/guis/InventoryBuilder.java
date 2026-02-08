package com.jankominek.disenchantment.guis;

import org.bukkit.inventory.Inventory;

/**
 * Utility class for populating a Bukkit {@link Inventory} with an array of {@link GUIItem} elements.
 */
public class InventoryBuilder {
    /**
     * Places each GUI item into its designated slot within the given inventory.
     *
     * @param inventory the inventory to populate
     * @param items     the array of GUI items to place
     * @return the populated inventory
     */
    public static Inventory fillItems(final Inventory inventory, final GUIItem[] items) {
        for (GUIItem item : items) {
            inventory.setItem(item.getSlot(), item.getItem());
        }

        return inventory;
    }
}
