package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.listeners.AnvilSessionListener;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests for the pending blank-book removal window: the anvil stays "operation pending" until the
 * deferred removal ran (or the anvil was closed), and the removal only ever touches slot 1 when it
 * is empty or still holds the same book.
 */
class AnvilPendingRemovalTest extends DisenchantmentTestBase {

    private AnvilInventory anvilWithSlot1(ItemStack slot1) {
        AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(anvil.getItem(1)).thenReturn(slot1);
        return anvil;
    }

    private ItemStack books(int amount) {
        return new ItemStack(Material.BOOK, amount);
    }

    // -> pending flag lifecycle

    @Test
    void givenScheduledRemoval_whenTwoTicksPass_thenPendingClearsAndStackIsDecremented() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(null); // EnchantsSquared / vanilla already emptied the slot

        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(3));
        assertTrue(AnvilEventGuards.isOperationPending(anvil), "Anvil must be pending right after scheduling");

        server.getScheduler().performTicks(2);

        assertFalse(AnvilEventGuards.isOperationPending(anvil), "Pending flag must clear once the removal ran");
        ArgumentCaptor<ItemStack> written = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(anvil).setItem(eq(1), written.capture());
        assertEquals(2, written.getValue().getAmount(), "3 books minus the one consumed");
        assertEquals(Material.BOOK, written.getValue().getType());
    }

    @Test
    void givenSingleBook_whenRemovalRuns_thenSlotCleared() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(books(1));

        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(1));
        server.getScheduler().performTicks(2);

        Mockito.verify(anvil).setItem(1, null);
    }

    // -> closing the anvil settles immediately and only once

    @Test
    void givenPendingRemoval_whenAnvilClosedEarly_thenSettledOnceAndDeferredTaskIsNoOp() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(books(3)); // vanilla left the whole stack in the slot

        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(3));

        assertTrue(AnvilEventGuards.settlePendingRemoval(anvil), "First settle applies the removal");
        assertFalse(AnvilEventGuards.isOperationPending(anvil));
        assertFalse(AnvilEventGuards.settlePendingRemoval(anvil), "Second settle must be a no-op");

        server.getScheduler().performTicks(2);

        ArgumentCaptor<ItemStack> written = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(anvil, Mockito.times(1)).setItem(eq(1), written.capture());
        assertEquals(2, written.getValue().getAmount(), "Early close must leave the player with one book fewer");
    }

    // -> slot 1 changed meanwhile: never overwrite a foreign item, never resurrect more than is left

    @Test
    void givenForeignItemInSlot1_whenRemovalRuns_thenSlotLeftUntouched() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(new ItemStack(Material.DIAMOND));

        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(3));
        server.getScheduler().performTicks(2);

        Mockito.verify(anvil, Mockito.never()).setItem(eq(1), Mockito.any());
        assertFalse(AnvilEventGuards.isOperationPending(anvil));
    }

    @Test
    void givenFewerBooksLeftThanExpected_whenRemovalRuns_thenNoBooksAreCreated() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(books(1));

        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(5));
        server.getScheduler().performTicks(2);

        ArgumentCaptor<ItemStack> written = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(anvil).setItem(eq(1), written.capture());
        assertEquals(1, written.getValue().getAmount(), "Must not write back more books than the slot still holds");
    }

    // -> lock listener

    @Test
    void givenPendingRemoval_whenOtherClickOrDragArrives_thenCancelledButOriginClickIsExempt() {
        PlayerMock player = server.addPlayer("TestPlayer");
        AnvilInventory anvil = anvilWithSlot1(null);
        InventoryClickEvent origin = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(origin.getInventory()).thenReturn(anvil);
        AnvilEventGuards.scheduleSecondItemRemoval(player, anvil, books(2), origin);

        AnvilSessionListener listener = new AnvilSessionListener();

        listener.onClick(origin);
        Mockito.verify(origin, Mockito.never()).setCancelled(true);

        InventoryClickEvent second = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(second.getInventory()).thenReturn(anvil);
        listener.onClick(second);
        Mockito.verify(second).setCancelled(true);

        InventoryDragEvent drag = Mockito.mock(InventoryDragEvent.class);
        Mockito.when(drag.getInventory()).thenReturn(anvil);
        listener.onDrag(drag);
        Mockito.verify(drag).setCancelled(true);

        server.getScheduler().performTicks(2);

        InventoryClickEvent afterwards = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(afterwards.getInventory()).thenReturn(anvil);
        listener.onClick(afterwards);
        Mockito.verify(afterwards, Mockito.never()).setCancelled(true);
    }

    @Test
    void givenNoPendingRemoval_whenNullInventoryChecked_thenNotLocked() {
        assertFalse(AnvilEventGuards.isOperationPending(null));
        assertFalse(AnvilEventGuards.isLockedFor(null, null));
    }
}
