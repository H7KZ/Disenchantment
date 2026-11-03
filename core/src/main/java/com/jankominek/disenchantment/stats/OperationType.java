package com.jankominek.disenchantment.stats;

/**
 * The type of anvil operation recorded in the stats system.
 */
public enum OperationType {
    /**
     * An enchantment was transferred from an item to a blank book.
     */
    DISENCHANT,
    /**
     * A multi-enchantment book was split into separate books.
     */
    SHATTER,
    /**
     * Enchantments were extracted from an item onto a plain book on the grindstone.
     */
    GRIND_EXTRACT,
    /**
     * Enchantments were transferred from one item to another on the grindstone.
     */
    GRIND_TRANSFER
}
