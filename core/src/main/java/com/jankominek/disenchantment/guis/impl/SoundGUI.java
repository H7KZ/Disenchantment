package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.types.AnvilFeature;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUI for configuring anvil sound settings for a given {@link AnvilFeature}.
 * Allows toggling sound on/off and adjusting volume and pitch values.
 */
public class SoundGUI implements InventoryHolder {
	private final AnvilFeature feature;
	private final GUIItem[] items;
	private final Inventory inventory;

	/**
	 * Constructs the sound GUI for the specified anvil feature, creating and populating the inventory.
	 *
	 * @param feature the anvil feature this GUI configures
	 */
	public SoundGUI(AnvilFeature feature) {
		this.feature = feature;

		this.items = ArrayUtils.addAll(
				GUIBorderComponent.border9x3(new Integer[]{0}),
				new GUIItem(
						0,
						GUIComponent.back(),
						event -> {
							event.setCancelled(true);
							event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
						}
				),
				GUIBorderComponent.border(10),
				new GUIItem(
						11,
						isSoundEnabled() ? soundEnabled() : soundDisabled(),
						event -> {
							event.setCancelled(true);

							boolean anvilSoundEnabled = !isSoundEnabled();

							setSoundEnabled(anvilSoundEnabled);

							event.setCurrentItem(anvilSoundEnabled ? soundEnabled() : soundDisabled());
						}
				),
				GUIBorderComponent.border(12),
				GUIBorderComponent.border(13),
				new GUIItem(
						14,
						volume(
								getVolume(),
								Math.min((int) (getVolume() * 10), 64)
						),
						event -> {
							event.setCancelled(true);

							double volume = getVolume();

							switch (event.getClick()) {
								case LEFT: {
									volume = PrecisionUtils.round(volume + 0.1, 1);
									break;
								}
								case RIGHT: {
									volume = PrecisionUtils.round(volume - 0.1, 1);
									break;
								}
								default:
									return;
							}

							setVolume(volume);

							event.setCurrentItem(volume(volume, Math.min((int) (volume * 10), 64)));
						}
				),
				new GUIItem(
						15,
						pitch(
								getPitch(),
								Math.min((int) (getPitch() * 10), 64)
						),
						event -> {
							event.setCancelled(true);

							double pitch = getPitch();

							switch (event.getClick()) {
								case LEFT: {
									pitch = PrecisionUtils.round(pitch + 0.1, 1);
									break;
								}
								case RIGHT: {
									pitch = PrecisionUtils.round(pitch - 0.1, 1);
									break;
								}
								default:
									return;
							}

							setPitch(pitch);

							event.setCurrentItem(pitch(pitch, Math.min((int) (pitch * 10), 64)));
						}
				),
				GUIBorderComponent.border(16),
				GUIBorderComponent.border(17)
		);

		String title = switch (feature) {
			case DISENCHANTMENT -> I18n.GUI.Sound.Disenchantment.inventory();
			case SHATTERMENT -> I18n.GUI.Sound.Shatterment.inventory();
		};

		Inventory inv = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize(title));

		this.inventory = InventoryBuilder.fillItems(inv, this.items);
	}

	private boolean isSoundEnabled() {
		return switch (feature) {
			case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.isEnabled();
			case SHATTERMENT -> Config.Shatterment.Anvil.Sound.isEnabled();
		};
	}

	private void setSoundEnabled(boolean value) {
		Config.beginBatch();
		try {
			switch (feature) {
				case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.setEnabled(value);
				case SHATTERMENT -> Config.Shatterment.Anvil.Sound.setEnabled(value);
			}
		} finally {
			Config.commitBatch();
		}
	}

	private double getVolume() {
		return switch (feature) {
			case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.getVolume();
			case SHATTERMENT -> Config.Shatterment.Anvil.Sound.getVolume();
		};
	}

	private void setVolume(double value) {
		Config.beginBatch();
		try {
			switch (feature) {
				case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.setVolume(value);
				case SHATTERMENT -> Config.Shatterment.Anvil.Sound.setVolume(value);
			}
		} finally {
			Config.commitBatch();
		}
	}

	private double getPitch() {
		return switch (feature) {
			case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.getPitch();
			case SHATTERMENT -> Config.Shatterment.Anvil.Sound.getPitch();
		};
	}

	private void setPitch(double value) {
		Config.beginBatch();
		try {
			switch (feature) {
				case DISENCHANTMENT -> Config.Disenchantment.Anvil.Sound.setPitch(value);
				case SHATTERMENT -> Config.Shatterment.Anvil.Sound.setPitch(value);
			}
		} finally {
			Config.commitBatch();
		}
	}

	private org.bukkit.inventory.ItemStack soundEnabled() {
		return switch (feature) {
			case DISENCHANTMENT -> GUIComponent.Sound.Disenchantment.enabled();
			case SHATTERMENT -> GUIComponent.Sound.Shatterment.enabled();
		};
	}

	private org.bukkit.inventory.ItemStack soundDisabled() {
		return switch (feature) {
			case DISENCHANTMENT -> GUIComponent.Sound.Disenchantment.disabled();
			case SHATTERMENT -> GUIComponent.Sound.Shatterment.disabled();
		};
	}

	private org.bukkit.inventory.ItemStack volume(double volume, int amount) {
		return switch (feature) {
			case DISENCHANTMENT -> GUIComponent.Sound.Disenchantment.volume(volume, amount);
			case SHATTERMENT -> GUIComponent.Sound.Shatterment.volume(volume, amount);
		};
	}

	private org.bukkit.inventory.ItemStack pitch(double pitch, int amount) {
		return switch (feature) {
			case DISENCHANTMENT -> GUIComponent.Sound.Disenchantment.pitch(pitch, amount);
			case SHATTERMENT -> GUIComponent.Sound.Shatterment.pitch(pitch, amount);
		};
	}

	/**
	 * Returns the anvil feature this GUI is configured for.
	 *
	 * @return the anvil feature
	 */
	public AnvilFeature getFeature() {
		return feature;
	}

	/**
	 * Delegates inventory click events to the appropriate GUI item handler based on the clicked slot.
	 *
	 * @param event the inventory click event
	 */
	public void onInventoryClick(InventoryClickEvent event) {
		for (GUIItem item : this.items) {
			if (item.getSlot() == event.getSlot()) item.onClick(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Inventory getInventory() {
		return this.inventory;
	}
}
