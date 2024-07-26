package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.utils.PrecisionUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static cz.kominekjan.disenchantment.config.Config.*;

public class SoundGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(new Integer[]{0}),
            new GUIItem(0, DefaultGUIElements.backItem(), event -> {
                event.setCancelled(true);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
            }),
            DefaultGUIElements.border(10),
            new GUIItem(11, getEnableAnvilSound() ? DefaultGUIElements.enabledAnvilSoundItem() : DefaultGUIElements.disabledAnvilSoundItem(), event -> {
                event.setCancelled(true);

                boolean anvilSoundEnabled = !getEnableAnvilSound();

                setEnableAnvilSound(anvilSoundEnabled);

                event.setCurrentItem(anvilSoundEnabled ? DefaultGUIElements.enabledAnvilSoundItem() : DefaultGUIElements.disabledAnvilSoundItem());
            }),
            DefaultGUIElements.border(12),
            DefaultGUIElements.border(13),
            new GUIItem(14, DefaultGUIElements.anvilSoundVolumeItem(getAnvilSoundVolume(), Math.min((int) (getAnvilSoundVolume() * 10), 64)), event -> {
                event.setCancelled(true);

                ClickType clickType = event.getClick();

                if (clickType.isLeftClick()) {
                    setAnvilSoundVolume(PrecisionUtil.round(getAnvilSoundVolume() + 0.1, 1));
                } else if (clickType.isRightClick()) {
                    setAnvilSoundVolume(PrecisionUtil.round(getAnvilSoundVolume() - 0.1, 1));
                }

                event.setCurrentItem(DefaultGUIElements.anvilSoundVolumeItem(getAnvilSoundVolume(), Math.min((int) (getAnvilSoundVolume() * 10), 64)));
            }),
            new GUIItem(15, DefaultGUIElements.anvilSoundPitchItem(getAnvilSoundPitch(), Math.min((int) (getAnvilSoundPitch() * 10), 64)), event -> {
                event.setCancelled(true);

                ClickType clickType = event.getClick();

                if (clickType.isLeftClick()) {
                    setAnvilSoundPitch(PrecisionUtil.round(getAnvilSoundPitch() + 0.1, 1));
                } else if (clickType.isRightClick()) {
                    setAnvilSoundPitch(PrecisionUtil.round(getAnvilSoundPitch() - 0.1, 1));
                }

                event.setCurrentItem(DefaultGUIElements.anvilSoundPitchItem(getAnvilSoundPitch(), Math.min((int) (getAnvilSoundPitch() * 10), 64)));
            }),
            DefaultGUIElements.border(16),
            DefaultGUIElements.border(17)
    );
    private final Integer size = 27;
    private final String title = "Anvil sound settings";
    private final Inventory inventory;

    public SoundGUI() {
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
