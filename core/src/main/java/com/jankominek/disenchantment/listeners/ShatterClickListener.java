package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.events.ShatterClickEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.EventExecutor;

import static com.jankominek.disenchantment.Disenchantment.plugin;
import static org.bukkit.Bukkit.getServer;

public class ShatterClickListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

    public ShatterClickListener(EventPriority priority) {
        getServer().getPluginManager().registerEvent(
                InventoryClickEvent.class,
                listener,
                priority,
                this,
                plugin,
                false
        );
    }

    @Override
    public void execute(Listener l, Event e) throws EventException {
        ShatterClickEvent.onEvent(e);
    }
}
