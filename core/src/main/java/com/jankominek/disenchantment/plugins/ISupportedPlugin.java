package com.jankominek.disenchantment.plugins;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Adapter interface for third-party enchantment plugins (e.g. ExcellentEnchants,
 * AdvancedEnchantments, UberEnchant).
 *
 * <p>Each supported external plugin provides an implementation that translates its
 * enchantments into a common {@link IPluginEnchantment} representation so the
 * Disenchantment plugin can remove or transfer them.</p>
 */
public interface ISupportedPlugin {
    /**
     * Returns the name of the third-party plugin as registered on the server.
     *
     * @return the plugin name
     */
    String getName();

    /**
     * Retrieves all enchantments from this third-party plugin that are present on the given item.
     *
     * @param item the item to inspect
     * @return a list of plugin-specific enchantments found on the item
     */
    List<IPluginEnchantment> getItemEnchantments(ItemStack item);

    /**
     * Called when this plugin adapter is activated during server startup.
     * Implementations may use this hook to perform initialisation.
     */
    default void activate() {
    }
}
