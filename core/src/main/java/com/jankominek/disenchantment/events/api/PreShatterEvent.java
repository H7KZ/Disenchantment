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
 * Fired immediately before a book-shattering (splitting) operation is executed —
 * after permission and XP checks pass, but before economy is charged or any
 * enchantments are moved between books.
 *
 * <p>Cancelling this event prevents the shattering from completing. The player's
 * XP and economy balance are <em>not</em> affected when cancelled.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventHandler
 * public void onPreShatter(PreShatterEvent event) {
 *     if (someCondition(event.getPlayer())) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 */
public class PreShatterEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final ItemStack sourceItem;
    private final List<IPluginEnchantment> enchantments;

    /**
     * @param player       the player performing the shattering
     * @param sourceItem   a snapshot of the enchanted book in anvil slot 0
     * @param enchantments the enchantments being split off onto the result book (the chosen subset,
     *                     plus any DELETE-state enchantments destroyed on the source)
     */
    public PreShatterEvent(Player player, ItemStack sourceItem, List<IPluginEnchantment> enchantments) {
        this.player = player;
        this.sourceItem = sourceItem;
        this.enchantments = enchantments;
    }

    /**
     * @return the player performing the shattering
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return a snapshot of the enchanted book in anvil slot 0 (not a live reference)
     */
    public ItemStack getSourceItem() {
        return sourceItem;
    }

    /**
     * @return the enchantments being split off onto the result book — the subset chosen for this
     * operation, not every enchantment on the source book (DELETE-state enchantments that will be
     * destroyed on the source are listed too); removing entries from this list prevents those
     * enchantments from being removed. The economy charge only covers the split-off subset.
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
