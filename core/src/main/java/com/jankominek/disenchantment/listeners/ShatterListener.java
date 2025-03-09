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

public class ShatterListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

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

    @Override
    public void execute(Listener l, Event e) throws EventException {
        ShatterEvent.onEvent(e);
    }
}
