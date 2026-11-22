package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.DisenchantEvent;
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
 * Registers and executes the disenchantment prepare-anvil event listener.
 * Uses the Bukkit {@link EventExecutor} pattern to allow configurable event priority
 * at registration time, delegating handling to {@link DisenchantEvent}.
 */
public class DisenchantListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

    /**
     * Creates a new listener and registers it for {@link PrepareAnvilEvent}
     * at the specified priority, and registers the shared {@link AnvilSessionListener}.
     *
     * @param priority the Bukkit event priority to register with
     */
    public DisenchantListener(EventPriority priority) {
        getServer().getPluginManager().registerEvent(
                PrepareAnvilEvent.class,
                listener,
                priority,
                this,
                plugin,
                false
        );

        // Shared per-anvil-session bookkeeping (pending blank-book removal lock, bypass-cost cleanup).
        // Registered here, exactly once per (re)registration of the anvil listeners, because the
        // shatter listener constructors run alongside this one and must not duplicate it.
        getServer().getPluginManager().registerEvents(new AnvilSessionListener(), plugin);
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link DisenchantEvent#onEvent(Event)}.
     */
    @Override
    public void execute(@NotNull Listener l, @NotNull Event e) throws EventException {
        DisenchantEvent.onEvent(e);
    }
}
