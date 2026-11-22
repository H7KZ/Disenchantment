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
 * Fired immediately before an erase operation is executed — after permission and XP checks pass, but
 * before economy is charged or anything is removed from the anvil.
 *
 * <p>Cancelling this event prevents the erase from completing. The player's XP, money and items are
 * <em>not</em> affected when cancelled.</p>
 */
public class PreEraseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final ItemStack sourceItem;
    private final List<IPluginEnchantment> enchantments;
    private final ItemStack catalyst;

    /**
     * @param player       the player performing the erase
     * @param sourceItem   a snapshot of the enchanted item in anvil slot 0
     * @param enchantments the enchantments that will be erased from the item
     * @param catalyst     a snapshot of the catalyst stack in anvil slot 1
     */
    public PreEraseEvent(Player player, ItemStack sourceItem, List<IPluginEnchantment> enchantments, ItemStack catalyst) {
        this.player = player;
        this.sourceItem = sourceItem;
        this.enchantments = enchantments;
        this.catalyst = catalyst;
    }

    /**
     * @return the player performing the erase
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return a snapshot of the item in anvil slot 0 (not a live reference)
     */
    public ItemStack getSourceItem() {
        return sourceItem;
    }

    /**
     * @return the enchantments that will be erased; removing entries from this list keeps those
     * enchantments on the item
     */
    public List<IPluginEnchantment> getEnchantments() {
        return enchantments;
    }

    /**
     * @return a snapshot of the catalyst stack in anvil slot 1 (not a live reference)
     */
    public ItemStack getCatalyst() {
        return catalyst;
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

    /**
     * Required by Bukkit's event system for handler registration.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
