package cz.kominekjan.disenchantment.guis.impl;

import cz.kominekjan.disenchantment.guis.GUIItem;
import cz.kominekjan.disenchantment.guis.InventoryBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class NavigationGUI implements InventoryHolder {
    private static final GUIItem[] items = ArrayUtils.addAll(
            DefaultGUIElements.border9x3(),
            new GUIItem(13, new ItemStack(Material.BARREL), event -> {
                event.getWhoClicked().sendMessage("Disenchanting");
                event.setCancelled(true);
            })
    );
    private final Integer size = 27;
    private final String title = "Disenchantment";
    private final Inventory inventory;

    public NavigationGUI() {
        Inventory inv = Bukkit.createInventory(this, this.size, this.title);

        this.inventory = InventoryBuilder.fillItems(inv, items);
    }

    public static void onInventoryClick(InventoryClickEvent event) {
        for (GUIItem item : items) {
            if (item.getSlot() == event.getSlot()) item.onClick(event);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
