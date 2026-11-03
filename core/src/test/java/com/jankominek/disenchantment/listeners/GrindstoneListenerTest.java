package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.FakePrepareEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for how the grindstone listeners are wired: PrepareGrindstoneEvent is resolved by class name and
 * the feature degrades to a tick-based refresh when the server does not have it.
 */
class GrindstoneListenerTest extends DisenchantmentTestBase {
    private static final Function<String, Class<?>> NO_CLASSES = name -> null;

    private final ItemStack[] slots = new ItemStack[3];

    private GrindstoneInventory grindstone(Player viewer, ItemStack slot0, ItemStack slot1) {
        slots[0] = slot0;
        slots[1] = slot1;
        slots[2] = null;
        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        Mockito.when(inventory.getType()).thenReturn(InventoryType.GRINDSTONE);
        Mockito.when(inventory.getViewers()).thenReturn(List.of(viewer));
        Mockito.when(inventory.getItem(Mockito.anyInt())).thenAnswer(inv -> slots[(int) inv.getArgument(0)]);
        Mockito.doAnswer(inv -> {
            slots[(int) inv.getArgument(0)] = inv.getArgument(1);
            return null;
        }).when(inventory).setItem(Mockito.anyInt(), Mockito.any());
        return inventory;
    }

    private InventoryClickEvent clickOn(Player player, Inventory top, int rawSlot) {
        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(event.getWhoClicked()).thenReturn(player);
        Mockito.when(event.getInventory()).thenReturn(top);
        Mockito.when(event.getRawSlot()).thenReturn(rawSlot);
        Mockito.when(event.getSlot()).thenReturn(rawSlot);
        Mockito.when(event.getClick()).thenReturn(ClickType.LEFT);
        return event;
    }

    private ItemStack enchantedSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        return sword;
    }

    // -> class resolution

    @Test
    void givenNeitherPrepareClass_whenLookup_thenNull() throws Exception {
        assertNull(GrindstoneListener.findPrepareEventClass(NO_CLASSES));
    }

    @Test
    void givenBothClasses_whenLookup_thenSpigotClassWins() throws Exception {
        Class<? extends Event> found = GrindstoneListener.findPrepareEventClass(name -> FakePrepareEvent.class);

        assertSame(FakePrepareEvent.class, found);
        assertEquals("org.bukkit.event.inventory.PrepareGrindstoneEvent", GrindstoneListener.PREPARE_EVENT_CLASSES[0]);
    }

    @Test
    void givenOnlyPaperLegacyClass_whenLookup_thenItIsUsed() throws Exception {
        Class<? extends Event> found = GrindstoneListener.findPrepareEventClass(
                name -> name.startsWith("com.destroystokyo") ? FakePrepareEvent.class : null);

        assertSame(FakePrepareEvent.class, found);
    }

    @Test
    void givenNonEventClass_whenLookup_thenIgnored() throws Exception {
        assertNull(GrindstoneListener.findPrepareEventClass(name -> String.class));
    }

    @Test
    void givenRealClassLookup_whenConstructed_thenNeverThrows() throws Exception {
        assertDoesNotThrow(() -> new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST));
    }

    // -> class-absent path: feature stays usable through the tick-based fallback

    @Test
    void givenNoPrepareEvent_whenConstructed_thenFlaggedUnavailable() throws Exception {
        GrindstoneListener listener = new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST, NO_CLASSES);

        assertFalse(listener.isPrepareEventAvailable());
    }

    @Test
    void givenNoPrepareEvent_whenGrindstoneClick_thenResultAppearsNextTick() throws Exception {
        setConfig("grindstone.enabled", true);
        GrindstoneListener listener = new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST, NO_CLASSES);
        PlayerMock player = server.addPlayer();
        GrindstoneInventory inventory = grindstone(player, enchantedSword(), new ItemStack(Material.BOOK));

        assertDoesNotThrow(() -> listener.execute(new Listener() {
        }, clickOn(player, inventory, 0)));
        assertNull(slots[2], "Not before the tick: vanilla finishes its own slot change first");

        server.getScheduler().performOneTick();

        assertNotNull(slots[2]);
        assertEquals(Material.ENCHANTED_BOOK, slots[2].getType());
    }

    @Test
    void givenNoPrepareEvent_whenFeatureDisabledOrOtherInventory_thenNothingScheduled() throws Exception {
        GrindstoneListener listener = new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST, NO_CLASSES);
        PlayerMock player = server.addPlayer();
        GrindstoneInventory inventory = grindstone(player, enchantedSword(), new ItemStack(Material.BOOK));

        listener.execute(new Listener() {
        }, clickOn(player, inventory, 0));
        server.getScheduler().performOneTick();
        assertNull(slots[2], "Disabled by default");

        setConfig("grindstone.enabled", true);
        Inventory chest = Mockito.mock(Inventory.class);
        Mockito.when(chest.getType()).thenReturn(InventoryType.CHEST);
        listener.execute(new Listener() {
        }, clickOn(player, chest, 0));
        server.getScheduler().performOneTick();
        assertNull(slots[2]);
    }

    @Test
    void givenPrepareEventAvailable_whenGrindstoneClick_thenNoFallbackRefresh() throws Exception {
        setConfig("grindstone.enabled", true);
        GrindstoneListener listener = new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST, name -> FakePrepareEvent.class);
        assertTrue(listener.isPrepareEventAvailable());
        PlayerMock player = server.addPlayer();
        GrindstoneInventory inventory = grindstone(player, enchantedSword(), new ItemStack(Material.BOOK));

        listener.execute(new Listener() {
        }, clickOn(player, inventory, 0));
        server.getScheduler().performOneTick();

        assertNull(slots[2], "The native prepare event does this job");
    }

    @Test
    void givenPrepareEventDelivered_whenExecute_thenRoutedToPrepareHandler() throws Exception {
        setConfig("grindstone.enabled", true);
        GrindstoneListener listener = new GrindstoneListener(EventPriority.HIGHEST, EventPriority.HIGHEST, name -> FakePrepareEvent.class);
        PlayerMock player = server.addPlayer();
        GrindstoneInventory inventory = grindstone(player, enchantedSword(), new ItemStack(Material.BOOK));
        FakePrepareEvent event = Mockito.mock(FakePrepareEvent.class);
        Mockito.when(event.getInventory()).thenReturn((Inventory) inventory);

        assertDoesNotThrow(() -> listener.execute(new Listener() {
        }, event));

        Mockito.verify(event).setResult(Mockito.argThat(item -> item != null && item.getType() == Material.ENCHANTED_BOOK));
    }
}
