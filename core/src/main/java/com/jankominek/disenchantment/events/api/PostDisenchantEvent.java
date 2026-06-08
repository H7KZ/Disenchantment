package com.jankominek.disenchantment.events.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PostDisenchantEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();
	private final Player player;
	private final ItemStack resultBook;
	private final ItemStack modifiedSourceItem;

	public PostDisenchantEvent(Player player, ItemStack resultBook, ItemStack modifiedSourceItem) {
		this.player = player;
		this.resultBook = resultBook;
		this.modifiedSourceItem = modifiedSourceItem;
	}

	public Player getPlayer() { return player; }
	public ItemStack getResultBook() { return resultBook; }
	public ItemStack getModifiedSourceItem() { return modifiedSourceItem; }
	@Override public HandlerList getHandlers() { return HANDLERS; }
	public static HandlerList getHandlerList() { return HANDLERS; }
}
