package com.jankominek.disenchantment.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mockito.Mockito;

import java.util.List;

/**
 * Test doubles for the grindstone: a three-slot in-memory {@link GrindstoneInventory} and a click event
 * builder. The inventory is stateful (setItem/getItem round-trip) so tests can assert on the final slot contents.
 */
final class GrindstoneTestSupport {
    private GrindstoneTestSupport() {
    }

    /**
     * An in-memory grindstone; slots 0-1 are inputs, slot 2 is the result.
     */
    static final class FakeGrindstone {
        final ItemStack[] slots = new ItemStack[3];
        final GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);

        FakeGrindstone(Player viewer, ItemStack slot0, ItemStack slot1, ItemStack slot2) {
            slots[0] = slot0;
            slots[1] = slot1;
            slots[2] = slot2;

            Mockito.when(inventory.getType()).thenReturn(InventoryType.GRINDSTONE);
            Mockito.when(inventory.getSize()).thenReturn(3);
            Mockito.when(inventory.getViewers()).thenReturn(List.of(viewer));
            Mockito.when(inventory.getItem(Mockito.anyInt())).thenAnswer(inv -> slots[(int) inv.getArgument(0)]);
            Mockito.doAnswer(inv -> {
                ItemStack item = inv.getArgument(1);
                slots[(int) inv.getArgument(0)] = (item == null || item.getType() == Material.AIR) ? null : item;
                return null;
            }).when(inventory).setItem(Mockito.anyInt(), Mockito.any());
        }

        ItemStack slot(int index) {
            return slots[index];
        }
    }

    /**
     * Builds a mocked click on {@code rawSlot} of {@code grindstone}.
     */
    static InventoryClickEvent click(Player player, FakeGrindstone grindstone, int rawSlot, ClickType type) {
        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(event.getWhoClicked()).thenReturn(player);
        Mockito.when(event.getInventory()).thenReturn(grindstone.inventory);
        Mockito.when(event.getRawSlot()).thenReturn(rawSlot);
        Mockito.when(event.getSlot()).thenReturn(rawSlot);
        Mockito.when(event.getClick()).thenReturn(type);
        if (rawSlot < 3) Mockito.when(event.getClickedInventory()).thenReturn((Inventory) grindstone.inventory);
        return event;
    }

    /**
     * Builds a mocked click on a slot of the player's own inventory while the grindstone is open.
     */
    static InventoryClickEvent clickPlayerInventory(Player player, FakeGrindstone grindstone, int playerSlot, ClickType type) {
        InventoryClickEvent event = click(player, grindstone, 3 + playerSlot, type);
        Mockito.when(event.getSlot()).thenReturn(playerSlot);
        Mockito.when(event.getClickedInventory()).thenReturn(player.getInventory());
        Mockito.when(event.getCurrentItem()).thenReturn(player.getInventory().getItem(playerSlot));
        return event;
    }
}
