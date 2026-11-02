package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.guis.GUIItem;
import com.jankominek.disenchantment.guis.impl.NavigationGUI;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GUIClickEventTest extends DisenchantmentTestBase {

    // Raw slot well inside the player's own inventory for a 27-slot top inventory.
    private static final int BOTTOM_RAW_SLOT = 27 + 9;

    private PlayerMock playerWithOpen(InventoryHolder gui, boolean grantGuiPermissions) {
        PlayerMock player = server.addPlayer();
        if (grantGuiPermissions) player.addAttachment(plugin, "disenchantment.gui", true);
        player.openInventory(gui.getInventory());
        return player;
    }

    private InventoryClickEvent click(PlayerMock player, int rawSlot, ClickType type, InventoryAction action) {
        InventoryClickEvent event = new InventoryClickEvent(player.getOpenInventory(),
                SlotType.CONTAINER, rawSlot, type, action);
        server.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Makes {@code slot} unhandled by moving its GUIItem to an out-of-range slot (items are looked up by slot).
     */
    private static void removeHandler(Object gui, int slot) throws Exception {
        Field f = gui.getClass().getDeclaredField("items");
        f.setAccessible(true);
        GUIItem[] items = (GUIItem[]) f.get(gui);
        for (int i = 0; i < items.length; i++) {
            if (items[i].getSlot() == slot) items[i] = new GUIItem(999, items[i].getItem(), e -> {
            });
        }
    }

    @Test
    void shiftClickFromPlayerInventory_whileGuiOpen_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);

        InventoryClickEvent event = click(player, BOTTOM_RAW_SLOT, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);

        assertTrue(event.isCancelled(), "shift-click from the bottom inventory must not move items into the GUI");
    }

    @Test
    void doubleClickCollectToCursor_whileGuiOpen_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);

        InventoryClickEvent event = click(player, BOTTOM_RAW_SLOT, ClickType.DOUBLE_CLICK, InventoryAction.COLLECT_TO_CURSOR);

        assertTrue(event.isCancelled(), "collect-to-cursor must not be able to pull GUI icons");
    }

    @Test
    void numberKeySwap_whileGuiOpen_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);

        assertTrue(click(player, BOTTOM_RAW_SLOT, ClickType.NUMBER_KEY, InventoryAction.HOTBAR_SWAP).isCancelled());
        assertTrue(click(player, 10, ClickType.NUMBER_KEY, InventoryAction.HOTBAR_SWAP).isCancelled());
    }

    @Test
    void clickOnEmptyGuiSlot_isCancelled() throws Exception {
        NavigationGUI gui = new NavigationGUI();
        int empty = 10;
        removeHandler(gui, empty);
        PlayerMock player = playerWithOpen(gui, true);
        player.setItemOnCursor(new ItemStack(Material.DIAMOND));

        InventoryClickEvent event = click(player, empty, ClickType.LEFT, InventoryAction.PLACE_ALL);

        assertTrue(event.isCancelled(), "placing an item into an empty GUI slot must be cancelled");
    }

    @Test
    void clickOutsideWindow_whileGuiOpen_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);

        InventoryClickEvent event = click(player, -999, ClickType.LEFT, InventoryAction.DROP_ALL_CURSOR);

        assertTrue(event.isCancelled());
    }

    @Test
    void permissionDenied_clickIsStillCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), false);

        InventoryClickEvent event = click(player, 10, ClickType.LEFT, InventoryAction.PICKUP_ALL);

        assertTrue(event.isCancelled(), "denied clicks must not fall through uncancelled");
    }

    @Test
    void permissionDenied_shiftClickFromBottom_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), false);

        assertTrue(click(player, BOTTOM_RAW_SLOT, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY).isCancelled());
    }

    @Test
    void drag_touchingGuiInventory_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);
        ItemStack stack = new ItemStack(Material.DIAMOND, 8);
        InventoryDragEvent event = new InventoryDragEvent(player.getOpenInventory(), stack, stack, false,
                Map.of(5, new ItemStack(Material.DIAMOND, 4), BOTTOM_RAW_SLOT, new ItemStack(Material.DIAMOND, 4)));

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void drag_onlyInPlayerInventory_whileGuiOpen_isCancelled() {
        PlayerMock player = playerWithOpen(new NavigationGUI(), true);
        ItemStack stack = new ItemStack(Material.DIAMOND, 8);
        InventoryDragEvent event = new InventoryDragEvent(player.getOpenInventory(), stack, stack, false,
                Map.of(BOTTOM_RAW_SLOT, new ItemStack(Material.DIAMOND, 8)));

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void foreignInventory_clicksAndDragsAreNotTouched() {
        PlayerMock player = server.addPlayer();
        player.openInventory(server.createInventory(null, 27));

        InventoryClickEvent click = click(player, BOTTOM_RAW_SLOT, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
        ItemStack stack = new ItemStack(Material.DIAMOND, 8);
        InventoryDragEvent drag = new InventoryDragEvent(player.getOpenInventory(), stack, stack, false,
                Map.of(3, new ItemStack(Material.DIAMOND, 8)));
        server.getPluginManager().callEvent(drag);

        assertFalse(click.isCancelled());
        assertFalse(drag.isCancelled());
    }
}
