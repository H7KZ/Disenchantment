package com.jankominek.disenchantment.types;

/**
 * Enumerates the types of anvil events handled by the plugin.
 * Used to distinguish between disenchantment (extracting enchantments from items)
 * and shatterment (splitting enchanted books) operations when calculating costs.
 */
public enum AnvilEventType {
    /**
     * Extracting enchantments from an item onto a book.
     */
    DISENCHANTMENT,
    /**
     * Splitting enchantments from one book onto another.
     */
    SHATTERMENT,
}
