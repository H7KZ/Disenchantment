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
 * Fired immediately before a grindstone item-to-item transfer is executed — after permission and XP
 * checks pass, but before economy is charged or anything is changed in the grindstone.
 *
 * <p>Cancelling this event prevents the transfer from completing. The player's XP and economy balance
 * are <em>not</em> affected when cancelled and the grindstone contents stay as they were.</p>
 */
public class PreGrindstoneTransferEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final ItemStack sourceItem;
    private final ItemStack targetItem;
    private final List<IPluginEnchantment> enchantments;

    /**
     * @param player       the player performing the transfer
     * @param sourceItem   a snapshot of the enchanted item in grindstone slot 0
     * @param targetItem   a snapshot of the item in grindstone slot 1 that receives the enchantments
     * @param enchantments the enchantments that will be moved (with the level they will have on the target)
     */
    public PreGrindstoneTransferEvent(Player player, ItemStack sourceItem, ItemStack targetItem, List<IPluginEnchantment> enchantments) {
        this.player = player;
        this.sourceItem = sourceItem;
        this.targetItem = targetItem;
        this.enchantments = enchantments;
    }

    /**
     * @return the player performing the transfer
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
     * @return a snapshot of the item in grindstone slot 1 (not a live reference)
     */
    public ItemStack getTargetItem() {
        return targetItem;
    }

    /**
     * @return the enchantments that will be moved to the target; removing entries from this list keeps
     * those enchantments on the source item and leaves them off the target
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
