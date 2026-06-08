package com.jankominek.disenchantment.events.api;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PreDisenchantEvent extends Event implements Cancellable {
	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled = false;
	private final Player player;
	private final ItemStack sourceItem;
	private final List<IPluginEnchantment> enchantments;

	public PreDisenchantEvent(Player player, ItemStack sourceItem, List<IPluginEnchantment> enchantments) {
		this.player = player;
		this.sourceItem = sourceItem;
		this.enchantments = enchantments;
	}

	public Player getPlayer() { return player; }
	public ItemStack getSourceItem() { return sourceItem; }
	public List<IPluginEnchantment> getEnchantments() { return enchantments; }
	@Override public boolean isCancelled() { return cancelled; }
	@Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
	@Override public HandlerList getHandlers() { return HANDLERS; }
	public static HandlerList getHandlerList() { return HANDLERS; }
}
