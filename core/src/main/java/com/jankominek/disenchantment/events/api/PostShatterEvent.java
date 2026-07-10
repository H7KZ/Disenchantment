package com.jankominek.disenchantment.events.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a book-shattering (splitting) operation has fully completed.
 * This event is informational only; it cannot be cancelled.
 * Use {@link PreShatterEvent} if you need to prevent the operation.
 */
public class PostShatterEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultBook;
    private final ItemStack modifiedSourceItem;
    private final int xpCost;
    private final double economyCost;

    public PostShatterEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem,
                             int xpCost, double economyCost) {
        this.player = player;
        this.resultBook = resultBook;
        this.modifiedSourceItem = modifiedSourceItem;
        this.xpCost = xpCost;
        this.economyCost = economyCost;
    }

    public Player getPlayer() { return player; }
    public ItemStack getResultBook() { return resultBook; }
    public ItemStack getModifiedSourceItem() { return modifiedSourceItem; }
    public int getXpCost() { return xpCost; }
    public double getEconomyCost() { return economyCost; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
