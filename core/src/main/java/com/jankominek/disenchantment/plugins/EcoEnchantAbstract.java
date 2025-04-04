package com.jankominek.disenchantment.plugins;

import com.jankominek.disenchantment.Disenchantment;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

public abstract class EcoEnchantAbstract implements ISupportedPlugin {

    @Override
    public void activate() {
        Bukkit.getScheduler().runTask(Disenchantment.plugin, this::delayedActivate);

    }

    // Just activate but later bc eco enchant is a... weirdly constructed plugin...
    public void delayedActivate() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Plugin ecoEnchant = pm.getPlugin(getName());
        if (ecoEnchant == null) {
            Disenchantment.logger.warning("Could not find eco enchant plugin but eco compatibility is enabled");
            return;
        }

        // Get the annoying listener
        RegisteredListener ecoListener = null;
        HandlerList preAnvilHandler = PrepareAnvilEvent.getHandlerList();
        for (RegisteredListener listener : preAnvilHandler.getRegisteredListeners()) {
            if (ecoEnchant != listener.getPlugin()) continue;

            ecoListener = listener;
            break;
        }

        if (ecoListener == null) {
            Disenchantment.logger.warning("Could not find eco enchant pre anvil listener");
            return;
        }

        // unregister then re register event so it is executed before disenchantment events
        preAnvilHandler.unregister(ecoListener);
        preAnvilHandler.register(ecoListener);
    }

}
