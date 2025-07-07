package com.jankominek.disenchantment.plugins;

import org.bukkit.inventory.ItemStack;

public interface IPluginEnchantment {
    String getKey();

    int getLevel();

    default ItemStack addToBook(ItemStack book) {
        return addToItem(book);
    }

    default ItemStack removeFromBook(ItemStack book) {
        return removeFromItem(book);
    }

    default ItemStack addToItem(ItemStack item) {
        return item;
    }

    default ItemStack removeFromItem(ItemStack item) {
        return item;
    }
}
