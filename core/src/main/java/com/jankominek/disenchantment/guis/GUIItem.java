package com.jankominek.disenchantment.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a clickable item within a GUI inventory, bound to a specific slot
 * with an associated click handler. Includes debounce logic to prevent rapid double-clicks.
 */
public class GUIItem {
    private final int slot;
    private final ItemStack item;
    private final IOnClickEvent onClick;

    private long lastClick = 0;

    /**
     * Constructs a new GUI item.
     *
     * @param slot    the inventory slot index this item occupies
     * @param item    the {@link ItemStack} to display
     * @param onClick the click event handler
     */
    public GUIItem(int slot, ItemStack item, IOnClickEvent onClick) {
        this.slot = slot;
        this.item = item;
        this.onClick = onClick;
    }

    /**
     * Returns the inventory slot index for this item.
     *
     * @return the slot index
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Returns the {@link ItemStack} displayed by this GUI item.
     *
     * @return the item stack
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Handles the inventory click event for this item. Cancels the event and returns
     * early if the click occurs within 100ms of the previous click to prevent double-clicks.
     *
     * @param event the inventory click event
     */
    public void onClick(InventoryClickEvent event) {
        // Avoid double-clicking
        long timeMilli = System.currentTimeMillis();

        if (timeMilli - lastClick < 100) {
            event.setCancelled(true);
            return;
        }

        lastClick = timeMilli;

        this.onClick.onClick(event);
    }
}
