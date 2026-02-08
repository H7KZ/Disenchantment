package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.ShatterEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.EventExecutor;

import static com.jankominek.disenchantment.Disenchantment.plugin;
import static org.bukkit.Bukkit.getServer;

/**
 * Registers and executes the shatterment prepare-anvil event listener.
 * Uses the Bukkit {@link EventExecutor} pattern to allow configurable event priority
 * at registration time, delegating handling to {@link ShatterEvent}.
 */
public class ShatterListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

    /**
     * Creates a new listener and registers it for {@link PrepareAnvilEvent}
     * at the specified priority.
     *
     * @param priority the Bukkit event priority to register with
     */
    public ShatterListener(EventPriority priority) {
        getServer().getPluginManager().registerEvent(
                PrepareAnvilEvent.class,
                listener,
                priority,
                this,
                plugin,
                false
        );
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link ShatterEvent#onEvent(Event)}.
     */
    @Override
    public void execute(Listener l, Event e) throws EventException {
        ShatterEvent.onEvent(e);
    }
}
