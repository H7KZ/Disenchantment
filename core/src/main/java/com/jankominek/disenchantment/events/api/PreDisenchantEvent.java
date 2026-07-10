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
 * Fired immediately before the disenchantment operation is executed — after
 * permission and XP checks pass, but before economy is charged or any
 * enchantments are removed from the source item.
 *
 * <p>Cancelling this event prevents the disenchantment from completing. The
 * player's XP and economy balance are <em>not</em> affected when cancelled.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventHandler
 * public void onPreDisenchant(PreDisenchantEvent event) {
 *     if (someCondition(event.getPlayer())) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 */
public class PreDisenchantEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final ItemStack sourceItem;
    private final List<IPluginEnchantment> enchantments;

    /**
     * @param player       the player performing the disenchantment
     * @param sourceItem   a snapshot of the enchanted item in anvil slot 0
     * @param enchantments the enchantments that will be transferred to the book
     */
    public PreDisenchantEvent(Player player, ItemStack sourceItem, List<IPluginEnchantment> enchantments) {
        this.player = player;
        this.sourceItem = sourceItem;
        this.enchantments = enchantments;
    }

    /**
     * @return the player performing the disenchantment
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
     * @return the enchantments that will be transferred; removing entries from this
     * list prevents those enchantments from being removed from the source item
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
