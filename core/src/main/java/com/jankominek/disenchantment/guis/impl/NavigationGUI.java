package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.GUIBorderComponent;
import com.jankominek.disenchantment.guis.GUIComponent;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.InventoryBuilder;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class NavigationGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            GUIBorderComponent.border9x3(),
            new GUIItem(
                    10,
                    Config.isPluginEnabled() ? GUIComponent.Navigation.Plugin.enabled() : GUIComponent.Navigation.Plugin.disabled(),
                    event -> {
                        event.setCancelled(true);

                        if (!PermissionGroupType.GUI_STATUS.hasPermission(event.getWhoClicked(), true)) return;

                        boolean pluginEnabled = !Config.isPluginEnabled();

                        Disenchantment.onToggle(pluginEnabled);

                        Config.setPluginEnabled(pluginEnabled);

                        event.setCurrentItem(pluginEnabled ? GUIComponent.Navigation.Plugin.enabled() : GUIComponent.Navigation.Plugin.disabled());
                    }
            ),
            new GUIItem(
                    11,
                    GUIComponent.Navigation.worlds(),
                    event -> {
                        event.setCancelled(true);

                        if (!PermissionGroupType.GUI_WORLDS.hasPermission(event.getWhoClicked(), true)) return;

                        event.getWhoClicked().openInventory(new WorldsGUI(0).getInventory());
                    }
            ),
            new GUIItem(
                    12,
                    GUIComponent.Navigation.repair(),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT: {
                                if (!PermissionGroupType.GUI_DISENCHANT_REPAIR.hasPermission(event.getWhoClicked(), true))
                                    return;

                                event.getWhoClicked().openInventory(new DisenchantmentRepairGUI().getInventory());
                            }
                            case RIGHT: {
                                if (!PermissionGroupType.GUI_SHATTER_REPAIR.hasPermission(event.getWhoClicked(), true))
                                    return;

                                event.getWhoClicked().openInventory(new ShattermentRepairGUI().getInventory());
                            }
                        }
                    }
            ),
            new GUIItem(
                    13,
                    GUIComponent.Navigation.enchantments(),
                    event -> {
                        event.setCancelled(true);

                        if (!PermissionGroupType.GUI_ENCHANTMENTS.hasPermission(event.getWhoClicked(), true)) return;

                        event.getWhoClicked().openInventory(new EnchantmentsGUI(0).getInventory());
                    }
            ),
            new GUIItem(
                    14,
                    GUIComponent.Navigation.materials(),
                    event -> {
                        event.setCancelled(true);

                        if (!PermissionGroupType.GUI_MATERIALS.hasPermission(event.getWhoClicked(), true)) return;

                        event.getWhoClicked().openInventory(new MaterialsGUI(0).getInventory());
                    }
            ),
            new GUIItem(
                    15,
                    GUIComponent.Navigation.sound(),
                    event -> {
                        event.setCancelled(true);

                        switch (event.getClick()) {
                            case LEFT: {
                                if (!PermissionGroupType.GUI_DISENCHANT_SOUND.hasPermission(event.getWhoClicked(), true))
                                    return;

                                event.getWhoClicked().openInventory(new DisenchantmentSoundGUI().getInventory());
                            }
                            case RIGHT: {
                                if (!PermissionGroupType.GUI_SHATTER_SOUND.hasPermission(event.getWhoClicked(), true))
                                    return;

                                event.getWhoClicked().openInventory(new ShattermentSoundGUI().getInventory());
                            }
                        }
                    }
            ),
            new GUIItem(
                    16,
                    GUIComponent.Navigation.spigot(),
                    event -> {
                        event.setCancelled(true);

                        event.getWhoClicked().sendMessage("https://www.spigotmc.org/resources/110741");
                        event.getWhoClicked().closeInventory();
                    }
            )
    );

    private final Inventory inventory;

    public NavigationGUI() {
        Inventory inventory = Bukkit.createInventory(this, 27, I18n.GUI.Navigation.inventory());

        this.inventory = InventoryBuilder.fillItems(inventory, this.items);
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
