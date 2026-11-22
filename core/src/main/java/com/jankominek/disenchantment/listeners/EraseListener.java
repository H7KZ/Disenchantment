package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.EraseEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jankominek.disenchantment.Disenchantment.plugin;
import static org.bukkit.Bukkit.getServer;

/**
 * Registers and executes the erase prepare-anvil event listener.
 * Uses the Bukkit {@link EventExecutor} pattern to allow configurable event priority
 * at registration time, delegating handling to {@link EraseEvent}.
 */
public class EraseListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

    /**
     * Creates a new listener and registers it for {@link PrepareAnvilEvent}
     * at the specified priority. The shared {@link AnvilSessionListener} is registered by {@link DisenchantListener}.
     *
     * @param priority the Bukkit event priority to register with
     */
    public EraseListener(EventPriority priority) {
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
     * Delegates to {@link EraseEvent#onEvent(Event)}.
     */
    @Override
    public void execute(@NotNull Listener l, @NotNull Event e) throws EventException {
        EraseEvent.onEvent(e);
    }
}
