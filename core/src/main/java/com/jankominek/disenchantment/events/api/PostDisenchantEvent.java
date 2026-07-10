package com.jankominek.disenchantment.events.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a disenchantment operation has fully completed.
 * This event is informational only; it cannot be cancelled.
 * Use {@link PreDisenchantEvent} if you need to prevent the operation.
 */
public class PostDisenchantEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultBook;
    private final ItemStack modifiedSourceItem;
    private final int xpCost;
    private final double economyCost;

    public PostDisenchantEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem,
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
    /** XP levels deducted from the player for this operation. */
    public int getXpCost() { return xpCost; }
    /** Money charged via economy for this operation (0.0 if economy disabled). */
    public double getEconomyCost() { return economyCost; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
