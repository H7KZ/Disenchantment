package com.jankominek.disenchantment.events.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.bukkit.inventory.ItemStack;

/**
 * Fired after a disenchantment operation has fully completed â€” enchantments have
 * been removed from the source item, the result book has been placed on the
 * player's cursor, XP has been deducted, and the anvil sound has played.
 *
 * <p>This event is informational only; it cannot be cancelled. Use
 * {@link PreDisenchantEvent} if you need to prevent the operation.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventHandler
 * public void onPostDisenchant(PostDisenchantEvent event) {
 *     logDisenchant(event.getPlayer(), event.getResultBook());
 * }
 * }</pre>
 */
public class PostDisenchantEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultBook;
    private final ItemStack modifiedSourceItem;

    /**
     * @param player             the player who performed the disenchantment
     * @param resultBook         the enchanted book placed on the player's cursor
     * @param modifiedSourceItem the source item after enchantments were removed
     */
    public PostDisenchantEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem) {
        this.player = player;
        this.resultBook = resultBook;
        this.modifiedSourceItem = modifiedSourceItem;
    }

    /**
     * @return the player who performed the disenchantment
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the enchanted book that was placed on the player's cursor
     */
    public ItemStack getResultBook() {
        return resultBook;
    }

    /**
     * @return the source item with enchantments removed (snapshot, not a live reference)
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
