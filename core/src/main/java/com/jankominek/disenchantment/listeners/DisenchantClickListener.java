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

public class DisenchantClickListener implements EventExecutor {
    private static final Listener listener = new Listener() {
    };

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

    @Override
    public void execute(Listener l, Event e) throws EventException {
        DisenchantClickEvent.onEvent(e);
    }
}
