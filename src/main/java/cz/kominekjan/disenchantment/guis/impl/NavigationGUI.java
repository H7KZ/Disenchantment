package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import static cz.kominekjan.disenchantment.utils.TextUtils.textWithPrefixError;

public class NavigationGUI implements InventoryHolder {
    private final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(),
            new GUIItem(10, Config.isPluginEnabled() ? DefaultGUIElements.enabledPluginItem() : DefaultGUIElements.disabledPluginItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.status")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                boolean pluginEnabled = !Config.isPluginEnabled();

                Disenchantment.toggle(pluginEnabled);
                Config.setPluginEnabled(pluginEnabled);

                event.setCurrentItem(pluginEnabled ? DefaultGUIElements.enabledPluginItem() : DefaultGUIElements.disabledPluginItem());
            }),
            new GUIItem(11, DefaultGUIElements.worldsItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.worlds")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                event.getWhoClicked().openInventory(new WorldsGUI(0).getInventory());
            }),
            new GUIItem(12, DefaultGUIElements.repairItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.disenchant_repair")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                event.getWhoClicked().openInventory(new DisenchantmentRepairGUI().getInventory());
            }),
            new GUIItem(13, DefaultGUIElements.enchantmentsItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.enchantments")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                event.getWhoClicked().openInventory(new EnchantmentsGUI(0).getInventory());
            }),
            new GUIItem(14, DefaultGUIElements.materialsItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.disenchant_materials")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                event.getWhoClicked().openInventory(new DisenchantMaterialsGUI(0).getInventory());
            }),
            new GUIItem(15, DefaultGUIElements.soundItem(), event -> {
                event.setCancelled(true);

                if (!event.getWhoClicked().hasPermission("disenchantment.gui.disenchant_sound")) {
                    event.getWhoClicked().sendMessage(textWithPrefixError("You don't have permission to use this feature."));
                    return;
                }

                event.getWhoClicked().openInventory(new DisenchantmentSoundGUI().getInventory());
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
