package com.jankominek.disenchantment.events.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a book-shattering (splitting) operation has fully completed â€”
 * one enchantment has been split off onto a new book, the original book has been
 * updated, XP has been deducted, and the anvil sound has played.
 *
 * <p>This event is informational only; it cannot be cancelled. Use
 * {@link PreShatterEvent} if you need to prevent the operation.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventHandler
 * public void onPostShatter(PostShatterEvent event) {
 *     logShatter(event.getPlayer(), event.getResultBook());
 * }
 * }</pre>
 */
public class PostShatterEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultBook;
    private final ItemStack modifiedSourceItem;

    /**
     * @param player             the player who performed the shattering
     * @param resultBook         the new book containing the split enchantment
     * @param modifiedSourceItem the original book with the split enchantment removed
     */
    public PostShatterEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem) {
        this.player = player;
        this.resultBook = resultBook;
        this.modifiedSourceItem = modifiedSourceItem;
    }

    /**
     * @return the player who performed the shattering
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the newly created book containing the single split enchantment
     */
    public ItemStack getResultBook() {
        return resultBook;
    }

    /**
     * @return the original book with one enchantment removed (snapshot, not a live reference)
     */
    public ItemStack getModifiedSourceItem() {
        return modifiedSourceItem;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
