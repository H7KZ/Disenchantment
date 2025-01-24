package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.guis.components.GUIElements;
import com.jankominek.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShattermentSoundGUI implements InventoryHolder {
    private final Integer size = 27;
    private final String title = "Shatterment | Sound";
    private final GUIItem[] items = ArrayUtils.addAll(
            GUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, GUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            GUIElements.border(10),
            new GUIItem(11, Config.Shatterment.Anvil.Sound.isEnabled() ? GUIElements.enabledShattermentAnvilSoundItem() : GUIElements.disabledShattermentAnvilSoundItem(), event -> {
                event.setCancelled(true);

                boolean anvilSoundEnabled = !Config.Shatterment.Anvil.Sound.isEnabled();

                Config.Shatterment.Anvil.Sound.setEnabled(anvilSoundEnabled);

                event.setCurrentItem(anvilSoundEnabled ? GUIElements.enabledShattermentAnvilSoundItem() : GUIElements.disabledShattermentAnvilSoundItem());
            }),
            GUIElements.border(12),
            GUIElements.border(13),
            new GUIItem(14, GUIElements.shattermentAnvilSoundVolumeItem(Config.Shatterment.Anvil.Sound.getVolume(), Math.min((int) (Config.Shatterment.Anvil.Sound.getVolume() * 10), 64)), event -> {
                event.setCancelled(true);

                double volume = Config.Shatterment.Anvil.Sound.getVolume();

                switch (event.getClick()) {
                    case LEFT:
                        volume = PrecisionUtils.round(volume + 0.1, 1);
                        break;
                    case RIGHT:
                        volume = PrecisionUtils.round(volume - 0.1, 1);
                        break;
                    default:
                        return;
                }

                Config.Shatterment.Anvil.Sound.setVolume(volume);

                event.setCurrentItem(GUIElements.shattermentAnvilSoundVolumeItem(volume, Math.min((int) (volume * 10), 64)));
            }),
            new GUIItem(15, GUIElements.shattermentAnvilSoundPitchItem(Config.Shatterment.Anvil.Sound.getPitch(), Math.min((int) (Config.Shatterment.Anvil.Sound.getPitch() * 10), 64)), event -> {
                event.setCancelled(true);

                double pitch = Config.Shatterment.Anvil.Sound.getPitch();

                switch (event.getClick()) {
                    case LEFT:
                        pitch = PrecisionUtils.round(pitch + 0.1, 1);
                        break;
                    case RIGHT:
                        pitch = PrecisionUtils.round(pitch - 0.1, 1);
                        break;
                    default:
                        return;
                }

                Config.Shatterment.Anvil.Sound.setPitch(pitch);

                event.setCurrentItem(GUIElements.shattermentAnvilSoundPitchItem(pitch, Math.min((int) (pitch * 10), 64)));
            }),
            GUIElements.border(16),
            GUIElements.border(17)
    );
    private final Inventory inventory;

    public ShattermentSoundGUI() {
        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        for (GUIItem item : this.items) {
            if (item.getSlot() == event.getSlot()) item.onClick(event);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
