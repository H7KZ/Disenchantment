package com.jankominek.disenchantment.guis.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.config.Config;
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
            GUIComponent.border9x3(),
            new GUIItem(10, Config.isPluginEnabled() ? GUIComponent.enabledPluginItem() : GUIComponent.disabledPluginItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGroupType.GUI_STATUS.hasPermission(event.getWhoClicked(), true)) return;

                boolean pluginEnabled = !Config.isPluginEnabled();

                Disenchantment.onToggle(pluginEnabled);
                Config.setPluginEnabled(pluginEnabled);

                event.setCurrentItem(pluginEnabled ? GUIComponent.enabledPluginItem() : GUIComponent.disabledPluginItem());
            }),
            new GUIItem(11, GUIComponent.worldsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGroupType.GUI_WORLDS.hasPermission(event.getWhoClicked(), true)) return;

                event.getWhoClicked().openInventory(new WorldsGUI(0).getInventory());
            }),
            new GUIItem(12, GUIComponent.repairItem(), event -> {
                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT:
                        if (!PermissionGroupType.GUI_DISENCHANT_REPAIR.hasPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new DisenchantmentRepairGUI().getInventory());
                        break;
                    case RIGHT:
                        if (!PermissionGroupType.GUI_SHATTER_REPAIR.hasPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new ShattermentRepairGUI().getInventory());
                        break;
                }
            }),
            new GUIItem(13, GUIComponent.enchantmentsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGroupType.GUI_ENCHANTMENTS.hasPermission(event.getWhoClicked(), true)) return;

                event.getWhoClicked().openInventory(new EnchantmentsGUI(0).getInventory());
            }),
            new GUIItem(14, GUIComponent.materialsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGroupType.GUI_MATERIALS.hasPermission(event.getWhoClicked(), true))
                    return;

                event.getWhoClicked().openInventory(new MaterialsGUI(0).getInventory());
            }),
            new GUIItem(15, GUIComponent.soundItem(), event -> {
                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT:
                        if (!PermissionGroupType.GUI_DISENCHANT_SOUND.hasPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new DisenchantmentSoundGUI().getInventory());
                        break;
                    case RIGHT:
                        if (!PermissionGroupType.GUI_SHATTER_SOUND.hasPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new ShattermentSoundGUI().getInventory());
                        break;
                }
            }),
            new GUIItem(16, GUIComponent.spigotItem(), event -> {
                event.setCancelled(true);

                event.getWhoClicked().sendMessage("https://www.spigotmc.org/resources/110741/");
                event.getWhoClicked().closeInventory();
            })
    );
    private final Integer size = 27;
    private final String title = "Navigation";
    private final Inventory inventory;

    public NavigationGUI() {
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
