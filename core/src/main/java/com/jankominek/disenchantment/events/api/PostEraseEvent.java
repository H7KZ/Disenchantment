package com.jankominek.disenchantment.events.api;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Fired after an erase operation has fully completed.
 * This event is informational only; it cannot be cancelled.
 * Use {@link PreEraseEvent} if you need to prevent the operation.
 */
public class PostEraseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultItem;
    private final List<IPluginEnchantment> erased;
    private final int xpCost;
    private final double economyCost;

    /**
     * @param player      the player who performed the erase
     * @param resultItem  the item the player received, without the erased enchantments (snapshot)
     * @param erased      the enchantments that were erased (snapshot)
     * @param xpCost      XP levels deducted (0 in creative mode)
     * @param economyCost money charged (0.0 if economy disabled or creative mode)
     */
    public PostEraseEvent(Player player, ItemStack resultItem, List<IPluginEnchantment> erased,
                          int xpCost, double economyCost) {
        this.player = player;
        this.resultItem = resultItem;
        this.erased = erased;
        this.xpCost = xpCost;
        this.economyCost = economyCost;
    }

    /**
     * Returns the player who performed the erase.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the item the player received, without the erased enchantments (snapshot, not a live reference).
     */
    public ItemStack getResultItem() {
        return resultItem;
    }

    /**
     * Returns the enchantments that were erased from the item.
     */
    public List<IPluginEnchantment> getErased() {
        return erased;
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
