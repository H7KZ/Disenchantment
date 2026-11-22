package com.jankominek.disenchantment.events.api;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Fired immediately before a grindstone extraction (enchanted item + plain book) is executed — after
 * permission and XP checks pass, but before economy is charged or anything is removed from the grindstone.
 *
 * <p>Cancelling this event prevents the extraction from completing. The player's XP and economy balance
 * are <em>not</em> affected when cancelled and the grindstone contents stay as they were.</p>
 */
public class PreGrindstoneExtractEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final ItemStack sourceItem;
    private final List<IPluginEnchantment> enchantments;

    /**
     * @param player       the player performing the extraction
     * @param sourceItem   a snapshot of the enchanted item in grindstone slot 0
     * @param enchantments the enchantments that will be moved to the book
     */
    public PreGrindstoneExtractEvent(Player player, ItemStack sourceItem, List<IPluginEnchantment> enchantments) {
        this.player = player;
        this.sourceItem = sourceItem;
        this.enchantments = enchantments;
    }

    /**
     * @return the player performing the extraction
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return a snapshot of the item in grindstone slot 0 (not a live reference)
     */
    public ItemStack getSourceItem() {
        return sourceItem;
    }

    /**
     * @return the enchantments that will be moved to the book; removing entries from this list keeps
     * those enchantments on the source item
     */
    public List<IPluginEnchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
