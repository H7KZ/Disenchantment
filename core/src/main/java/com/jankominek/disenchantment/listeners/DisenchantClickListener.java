package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.DisenchantClickEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.EventExecutor;

import static com.jankominek.disenchantment.Disenchantment.plugin;
import static org.bukkit.Bukkit.getServer;

/**
 * Registers and executes the disenchantment inventory-click event listener.
 * Uses the Bukkit {@link EventExecutor} pattern to allow configurable event priority
 * at registration time, delegating handling to {@link DisenchantClickEvent}.
 */
public class DisenchantClickListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

    /**
     * Creates a new listener and registers it for {@link InventoryClickEvent}
     * at the specified priority.
     *
     * @param priority the Bukkit event priority to register with
     */
    public DisenchantClickListener(EventPriority priority) {
        getServer().getPluginManager().registerEvent(
                InventoryClickEvent.class,
                listener,
                priority,
                this,
                plugin,
                false
        );
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link DisenchantClickEvent#onEvent(Event)}.
     */
    @Override
    public void execute(Listener l, Event e) throws EventException {
        DisenchantClickEvent.onEvent(e);
    }
}
