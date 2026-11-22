package com.jankominek.disenchantment.e2e.support.hooks;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A third-party listener on the plugin's cancellable Pre events, the way an API consumer would write one. The classes are
 * resolved through Disenchantment's class loader at runtime, so this plugin has no compile dependency on it. A Pre event
 * is cancelled while {@code cancel.<name>} is true in this plugin's live config (a test sets it with patchConfig).
 */
public final class E2eHooks extends JavaPlugin implements Listener {

    private static final String API = "com.jankominek.disenchantment.events.api.";
    private static final String[] PRE_EVENTS = {"PreDisenchantEvent", "PreShatterEvent", "PreEraseEvent",
            "PreGrindstoneExtractEvent", "PreGrindstoneTransferEvent"};

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        Plugin target = getServer().getPluginManager().getPlugin("Disenchantment");
        if (target == null) {
            getLogger().warning("Disenchantment is not installed; no hooks registered");
            return;
        }
        for (String name : PRE_EVENTS) {
            try {
                Class<? extends Event> type = (Class<? extends Event>) Class.forName(API + name, true, target.getClass().getClassLoader());
                getServer().getPluginManager().registerEvent(type, this, EventPriority.NORMAL, (listener, event) -> {
                    if (getConfig().getBoolean("cancel." + name, false) && event instanceof Cancellable cancellable) {
                        cancellable.setCancelled(true);
                    }
                }, this, false);
            } catch (ClassNotFoundException e) {
                getLogger().warning("no " + name + " in this Disenchantment build");
            }
        }
    }
}
