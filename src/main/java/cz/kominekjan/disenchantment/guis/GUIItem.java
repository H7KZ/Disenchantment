package cz.kominekjan.disenchantment.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIItem {
    private final Integer slot;
    private final ItemStack item;
    private final IOnClick onClick;

    public GUIItem(int slot, ItemStack item, IOnClick onClick) {
        this.slot = slot;
        this.item = item;
        this.onClick = onClick;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public void onClick(InventoryClickEvent event) {
        this.onClick.onClick(event);
    }
}
