package com.jankominek.disenchantment.events;

import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Test stand-in for {@code PrepareGrindstoneEvent} (absent from the 1.18 API the core compiles against):
 * an {@link InventoryEvent} with a {@code setResult(ItemStack)} method, mocked with Mockito.
 */
public abstract class FakePrepareEvent extends InventoryEvent {
    protected FakePrepareEvent() {
        super(null);
    }

    public abstract void setResult(ItemStack result);
}
