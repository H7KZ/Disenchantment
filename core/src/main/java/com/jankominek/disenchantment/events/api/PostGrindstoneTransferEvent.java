package com.jankominek.disenchantment.events.api;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Fired after a grindstone item-to-item transfer has fully completed.
 * This event is informational only; it cannot be cancelled.
 * Use {@link PreGrindstoneTransferEvent} if you need to prevent the operation.
 */
public class PostGrindstoneTransferEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack resultItem;
    private final ItemStack modifiedSourceItem;
    private final List<IPluginEnchantment> enchantments;
    private final int xpCost;
    private final double economyCost;

    /**
     * @param player             the player who performed the transfer
     * @param resultItem         the enriched target item the player received (snapshot)
     * @param modifiedSourceItem the source item after the moved enchantments were removed (snapshot)
     * @param enchantments       the enchantments that were moved from the source to the target
     * @param xpCost             XP levels deducted (0 in creative mode)
     * @param economyCost        money charged (0.0 if economy disabled or creative mode)
     */
    public PostGrindstoneTransferEvent(Player player, ItemStack resultItem, ItemStack modifiedSourceItem,
                                       List<IPluginEnchantment> enchantments, int xpCost, double economyCost) {
        this.player = player;
        this.resultItem = resultItem;
        this.modifiedSourceItem = modifiedSourceItem;
        this.enchantments = enchantments;
        this.xpCost = xpCost;
        this.economyCost = economyCost;
    }

    /**
     * Returns the player who performed the transfer.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the enriched target item the player received (snapshot, not a live reference).
     */
    public ItemStack getResultItem() {
        return resultItem;
    }

    /**
     * Returns the source item after the moved enchantments were removed (snapshot, not a live reference).
     */
    public ItemStack getModifiedSourceItem() {
        return modifiedSourceItem;
    }

    /**
     * Returns the enchantments that were moved from the source to the target (level as applied to the target).
     */
    public List<IPluginEnchantment> getEnchantments() {
        return enchantments;
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
