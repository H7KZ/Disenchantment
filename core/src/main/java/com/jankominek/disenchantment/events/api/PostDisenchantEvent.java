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

    /**
     * @param player             the player who performed the disenchantment
     * @param resultBook         the enchanted book the player received (snapshot)
     * @param modifiedSourceItem the source item after enchantments were removed (snapshot)
     * @param xpCost             XP levels deducted (0 in creative mode)
     * @param economyCost        money charged (0.0 if economy disabled or creative mode)
     */
    public PostDisenchantEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem,
                               int xpCost, double economyCost) {
        this.player = player;
        this.resultBook = resultBook;
        this.modifiedSourceItem = modifiedSourceItem;
        this.xpCost = xpCost;
        this.economyCost = economyCost;
    }

    /**
     * Returns the player who performed the disenchantment.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the enchanted book the player received (snapshot, not a live reference).
     */
    public ItemStack getResultBook() {
        return resultBook;
    }

    /**
     * Returns the source item after enchantments were removed (snapshot, not a live reference).
     */
    public ItemStack getModifiedSourceItem() {
        return modifiedSourceItem;
    }

    /**
     * XP levels deducted from the player for this operation.
     */
    public int getXpCost() {
        return xpCost;
    }

    /**
     * Money charged via economy for this operation (0.0 if economy disabled).
     */
    public double getEconomyCost() {
        return economyCost;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit's event system for handler registration.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
