package com.jankominek.disenchantment.guis;

import org.bukkit.inventory.Inventory;

public class InventoryBuilder {
    public static Inventory fillItems(final Inventory inventory, final GUIItem[] items) {
        for (GUIItem item : items) {
            inventory.setItem(item.getSlot(), item.getItem());
        }

        return inventory;
    }
}
