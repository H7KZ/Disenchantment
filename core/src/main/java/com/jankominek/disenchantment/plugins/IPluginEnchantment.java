package com.jankominek.disenchantment.plugins;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a single enchantment originating from a third-party plugin.
 *
 * <p>Provides operations to add or remove the enchantment from items and books,
 * enabling the Disenchantment plugin to manipulate enchantments regardless of
 * their source plugin.</p>
 */
public interface IPluginEnchantment {
    /**
     * Returns the unique key identifying this enchantment (typically a namespaced key).
     *
     * @return the enchantment key
     */
    String getKey();

    /**
     * Returns the level of this enchantment on the item it was read from.
     *
     * @return the enchantment level
     */
    int getLevel();

    /**
     * Adds this enchantment to an enchanted book.
     * Defaults to {@link #addToItem(ItemStack)}.
     *
     * @param book the enchanted book to add to
     * @return the modified book with the enchantment applied
     */
    default ItemStack addToBook(ItemStack book) {
        return addToItem(book);
    }

    /**
     * Removes this enchantment from an enchanted book.
     * Defaults to {@link #removeFromItem(ItemStack)}.
     *
     * @param book the enchanted book to remove from
     * @return the modified book with the enchantment removed
     */
    default ItemStack removeFromBook(ItemStack book) {
        return removeFromItem(book);
    }

    /**
     * Adds this enchantment to an item.
     *
     * @param item the item to enchant
     * @return the modified item with the enchantment applied
     */
    default ItemStack addToItem(ItemStack item) {
        return item;
    }

    /**
     * Removes this enchantment from an item.
     *
     * @param item the item to remove the enchantment from
     * @return the modified item with the enchantment removed
     */
    default ItemStack removeFromItem(ItemStack item) {
        return item;
    }
}
