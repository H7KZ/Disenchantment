package com.jankominek.disenchantment.types;

/**
 * Enumerates the anvil features provided by the plugin.
 * Used to identify which features are enabled or disabled in configuration.
 */
public enum AnvilFeature {
    /**
     * Disenchanting feature: extracting enchantments from items onto books.
     */
    DISENCHANTMENT,
    /**
     * Shattering feature: splitting enchanted books into individual ones.
     */
    SHATTERMENT,
}
