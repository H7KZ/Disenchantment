package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.AnvilEventGuards;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Per-anvil-session bookkeeping that is not tied to a configurable priority: it keeps the
 * deferred blank-book removal of {@link AnvilEventGuards#scheduleSecondItemRemoval} airtight.
 * <ul>
 *   <li>While a removal is pending, clicks and drags on that anvil are cancelled, so the player
 *   cannot pull the books or the source item out during the 2-tick window.</li>
 *   <li>Closing the anvil settles the pending removal immediately — before the server hands slot
 *   contents back to the player — so the consumed book cannot be duplicated by closing early.</li>
 * </ul>
 * Registered once from {@link DisenchantListener}.
 */
public class AnvilSessionListener implements Listener {

    /**
     * Cancels any click on an anvil that still has a pending blank-book removal, except the
     * click that started the operation.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (AnvilEventGuards.isLockedFor(e.getInventory(), e)) {
            DiagnosticUtils.debug("ANVIL", "Session: click during pending book removal → CANCELLED");
            e.setCancelled(true);
        }
    }

    /**
     * Cancels any drag on an anvil that still has a pending blank-book removal.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (AnvilEventGuards.isLockedFor(e.getInventory(), null)) {
            DiagnosticUtils.debug("ANVIL", "Session: drag during pending book removal → CANCELLED");
            e.setCancelled(true);
        }
    }

    /**
     * Settles a pending blank-book removal when the anvil is closed.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClose(InventoryCloseEvent e) {
        if (AnvilEventGuards.settlePendingRemoval(e.getInventory())) {
            DiagnosticUtils.debug("ANVIL", "Session: anvil closed with pending book removal → settled immediately");
        }
    }
}
