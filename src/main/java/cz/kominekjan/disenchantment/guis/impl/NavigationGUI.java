package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class NavigationGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(),
            new GUIItem(10, Config.isPluginEnabled() ? DefaultGUIElements.enabledPluginItem() : DefaultGUIElements.disabledPluginItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGoal.GUI_EDIT_STATUS.checkPermission(event.getWhoClicked(), true)) return;

                boolean pluginEnabled = !Config.isPluginEnabled();

                Disenchantment.toggle(pluginEnabled);
                Config.setPluginEnabled(pluginEnabled);

                event.setCurrentItem(pluginEnabled ? DefaultGUIElements.enabledPluginItem() : DefaultGUIElements.disabledPluginItem());
            }),
            new GUIItem(11, DefaultGUIElements.worldsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGoal.GUI_EDIT_ALLOWED_WORLDS.checkPermission(event.getWhoClicked(), true)) return;

                event.getWhoClicked().openInventory(new WorldsGUI(0).getInventory());
            }),
            new GUIItem(12, DefaultGUIElements.repairItem(), event -> {
                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT:
                        if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_REPAIR.checkPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new DisenchantmentRepairGUI().getInventory());
                        break;
                    case RIGHT:
                        if (!PermissionGoal.GUI_EDIT_SHATTERMENT_REPAIR.checkPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new ShattermentRepairGUI().getInventory());
                        break;
                }
            }),
            new GUIItem(13, DefaultGUIElements.enchantmentsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGoal.GUI_EDIT_ENCHANTMENT_STATES.checkPermission(event.getWhoClicked(), true)) return;

                event.getWhoClicked().openInventory(new EnchantmentsGUI(0).getInventory());
            }),
            new GUIItem(14, DefaultGUIElements.materialsItem(), event -> {
                event.setCancelled(true);

                if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_MATERIALS.checkPermission(event.getWhoClicked(), true))
                    return;

                event.getWhoClicked().openInventory(new DisenchantMaterialsGUI(0).getInventory());
            }),
            new GUIItem(15, DefaultGUIElements.soundItem(), event -> {
                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT:
                        if (!PermissionGoal.GUI_EDIT_DISENCHANTMENT_SOUND.checkPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new DisenchantmentSoundGUI().getInventory());
                        break;
                    case RIGHT:
                        if (!PermissionGoal.GUI_EDIT_SHATTERMENT_SOUND.checkPermission(event.getWhoClicked(), true))
                            return;

                        event.getWhoClicked().openInventory(new ShattermentSoundGUI().getInventory());
                        break;
                }
            }),
            new GUIItem(16, DefaultGUIElements.spigotItem(), event -> {
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
