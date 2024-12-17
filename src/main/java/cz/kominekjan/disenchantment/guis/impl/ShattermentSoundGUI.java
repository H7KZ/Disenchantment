package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.utils.PrecisionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShattermentSoundGUI implements InventoryHolder {
    private final Integer size = 27;
    private final String title = "Shatterment | Sound";
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, DefaultGUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            DefaultGUIElements.border(10),
            new GUIItem(11, Config.Shatterment.Anvil.Sound.isEnabled() ? DefaultGUIElements.enabledShattermentAnvilSoundItem() : DefaultGUIElements.disabledShattermentAnvilSoundItem(), event -> {
                event.setCancelled(true);

                boolean anvilSoundEnabled = !Config.Shatterment.Anvil.Sound.isEnabled();

                Config.Shatterment.Anvil.Sound.setEnabled(anvilSoundEnabled);

                event.setCurrentItem(anvilSoundEnabled ? DefaultGUIElements.enabledShattermentAnvilSoundItem() : DefaultGUIElements.disabledShattermentAnvilSoundItem());
            }),
            DefaultGUIElements.border(12),
            DefaultGUIElements.border(13),
            new GUIItem(14, DefaultGUIElements.shattermentAnvilSoundVolumeItem(Config.Shatterment.Anvil.Sound.getVolume(), Math.min((int) (Config.Shatterment.Anvil.Sound.getVolume() * 10), 64)), event -> {
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

                event.setCurrentItem(DefaultGUIElements.shattermentAnvilSoundVolumeItem(volume, Math.min((int) (volume * 10), 64)));
            }),
            new GUIItem(15, DefaultGUIElements.shattermentAnvilSoundPitchItem(Config.Shatterment.Anvil.Sound.getPitch(), Math.min((int) (Config.Shatterment.Anvil.Sound.getPitch() * 10), 64)), event -> {
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

                event.setCurrentItem(DefaultGUIElements.shattermentAnvilSoundPitchItem(pitch, Math.min((int) (pitch * 10), 64)));
            }),
            DefaultGUIElements.border(16),
            DefaultGUIElements.border(17)
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}