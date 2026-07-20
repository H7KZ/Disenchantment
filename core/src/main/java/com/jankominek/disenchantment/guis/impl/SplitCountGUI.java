package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.*;
import com.jankominek.disenchantment.types.SplitCountRange;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI for configuring the number of enchantments split off per shatterment operation.
 * Supports a fixed count (single value) or a random range (independent min/max bounds).
 */
public class SplitCountGUI implements InventoryHolder {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 64;

    private final GUIItem[] items;
    private final Inventory inventory;
    private final boolean fixed;

    /**
     * Constructs the split count GUI, reading the current mode and values from config.
     */
    public SplitCountGUI() {
        SplitCountRange range = Config.Shatterment.getSplitCount();
        this.fixed = range.isFixed();

        if (this.fixed) {
            this.items = ArrayUtils.addAll(
                    GUIBorderComponent.border9x3(new Integer[]{0, 13, 15}),
                    GUIBorderComponent.border(10),
                    GUIBorderComponent.border(11),
                    GUIBorderComponent.border(12),
                    GUIBorderComponent.border(14),
                    GUIBorderComponent.border(16),
                    new GUIItem(0, GUIComponent.back(), event -> {
                        event.setCancelled(true);

                        event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                    }),
                    new GUIItem(13, countItemFixed(range.min()), event -> {
                        event.setCancelled(true);

                        int value = clamp(range.min() + delta(event));
                        if (value == range.min()) return;

                        Config.Shatterment.setSplitCount(new SplitCountRange(value, value));

                        event.getWhoClicked().openInventory(new SplitCountGUI().getInventory());
                    }),
                    new GUIItem(15, modeItemFixed(), event -> {
                        event.setCancelled(true);

                        // Switching fixed → range: seed min/max from the current value, nudging
                        // max up (or min down, if already at the cap) so min != max persists as a range.
                        int newMax = clamp(range.min() + 1);
                        int newMin = newMax == range.min() ? clamp(range.min() - 1) : range.min();

                        Config.Shatterment.setSplitCount(new SplitCountRange(newMin, newMax));

                        event.getWhoClicked().openInventory(new SplitCountGUI().getInventory());
                    })
            );
        } else {
            this.items = ArrayUtils.addAll(
                    GUIBorderComponent.border9x3(new Integer[]{0, 11, 13, 15}),
                    GUIBorderComponent.border(10),
                    GUIBorderComponent.border(12),
                    GUIBorderComponent.border(14),
                    GUIBorderComponent.border(16),
                    new GUIItem(0, GUIComponent.back(), event -> {
                        event.setCancelled(true);

                        event.getWhoClicked().openInventory(new NavigationGUI().getInventory());
                    }),
                    new GUIItem(11, countItemMin(range.min()), event -> {
                        event.setCancelled(true);

                        int value = clamp(Math.min(range.min() + delta(event), range.max()));
                        if (value == range.min()) return;

                        Config.Shatterment.setSplitCount(new SplitCountRange(value, range.max()));

                        event.getWhoClicked().openInventory(new SplitCountGUI().getInventory());
                    }),
                    new GUIItem(13, modeItemRange(), event -> {
                        event.setCancelled(true);

                        Config.Shatterment.setSplitCount(new SplitCountRange(range.min(), range.min()));

                        event.getWhoClicked().openInventory(new SplitCountGUI().getInventory());
                    }),
                    new GUIItem(15, countItemMax(range.max()), event -> {
                        event.setCancelled(true);

                        int value = clamp(Math.max(range.max() + delta(event), range.min()));
                        if (value == range.max()) return;

                        Config.Shatterment.setSplitCount(new SplitCountRange(range.min(), value));

                        event.getWhoClicked().openInventory(new SplitCountGUI().getInventory());
                    })
            );
        }

        Inventory inv = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize(I18n.GUI.SplitCount.inventory()));

        this.inventory = InventoryBuilder.fillItems(inv, this.items);
    }

    private static int delta(InventoryClickEvent event) {
        return switch (event.getClick()) {
            case LEFT -> 1;
            case RIGHT -> -1;
            default -> 0;
        };
    }

    private static int clamp(int value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }

    private static ItemStack countItemFixed(int value) {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setAmount(clamp(value))
                .setDisplayName(I18n.GUI.SplitCount.Count.title())
                .setLore(I18n.GUI.SplitCount.Count.loreFixed(String.valueOf(value)))
                .addAllFlags()
                .build();
    }

    private static ItemStack countItemMin(int value) {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setAmount(clamp(value))
                .setDisplayName(I18n.GUI.SplitCount.Count.title())
                .setLore(I18n.GUI.SplitCount.Count.loreMin(String.valueOf(value)))
                .addAllFlags()
                .build();
    }

    private static ItemStack countItemMax(int value) {
        return new ItemBuilder(Material.DRAGON_BREATH)
                .setAmount(clamp(value))
                .setDisplayName(I18n.GUI.SplitCount.Count.title())
                .setLore(I18n.GUI.SplitCount.Count.loreMax(String.valueOf(value)))
                .addAllFlags()
                .build();
    }

    private static ItemStack modeItemFixed() {
        return new ItemBuilder(Material.LEVER)
                .setDisplayName(I18n.GUI.SplitCount.Mode.title())
                .setLore(I18n.GUI.SplitCount.Mode.loreFixed())
                .addAllFlags()
                .build();
    }

    private static ItemStack modeItemRange() {
        return new ItemBuilder(Material.COMPARATOR)
                .setDisplayName(I18n.GUI.SplitCount.Mode.title())
                .setLore(I18n.GUI.SplitCount.Mode.loreRange())
                .addAllFlags()
                .build();
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
