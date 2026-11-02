package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import com.jankominek.disenchantment.events.api.PreShatterEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Integration tests for ShatterClickEvent — simulates a player clicking the anvil
 * result slot during a book-splitting operation and asserts the handler strips the
 * split enchantment from the source book and delivers the result book.
 */
class ShatterClickEventTest extends DisenchantmentTestBase {

    private AnvilInventory lastMockAnvil;

    private InventoryClickEvent buildClickEvent(PlayerMock player, ItemStack slot0, ItemStack slot1, ItemStack slot2) {
        AnvilInventory mockAnvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(mockAnvil.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(mockAnvil.getItem(0)).thenReturn(slot0);
        Mockito.when(mockAnvil.getItem(1)).thenReturn(slot1);
        Mockito.when(mockAnvil.getItem(2)).thenReturn(slot2);
        Mockito.when(mockAnvil.getRepairCost()).thenReturn(0);
        Mockito.when(mockAnvil.getViewers()).thenReturn(List.of(player));
        lastMockAnvil = mockAnvil;

        List<Class<?>> proxyInterfaces = new ArrayList<>();
        proxyInterfaces.add(InventoryView.class);
        try {
            proxyInterfaces.add(Class.forName("org.bukkit.inventory.view.AnvilView"));
        } catch (ClassNotFoundException ignored) {
        }
        Object viewProxy = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                proxyInterfaces.toArray(new Class[0]),
                (proxy, method, args) -> {
                    if ("getPlayer".equals(method.getName())) return player;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType().isPrimitive()) return 0;
                    return null;
                });

        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(event.getWhoClicked()).thenReturn(player);
        Mockito.when(event.getInventory()).thenReturn(mockAnvil);
        Mockito.when(event.getSlot()).thenReturn(2);
        Mockito.when(event.getRawSlot()).thenReturn(2);
        Mockito.when(event.isShiftClick()).thenReturn(false);
        Mockito.when(event.getView()).thenReturn((InventoryView) viewProxy);
        return event;
    }

    private ItemStack enchantedBook(String... keyLevelPairs) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        for (int i = 0; i < keyLevelPairs.length; i += 2) {
            EnchantmentUtils.addStoredEnchantment(book, enchantment(keyLevelPairs[i]), Integer.parseInt(keyLevelPairs[i + 1]));
        }
        return book;
    }

    // -> issue #73: with a namespace-scoped adapter (Vane) active, the split enchantment
    //    must still be stripped from the source book. Otherwise the split book is delivered
    //    while the source keeps every enchantment — a dupe.

    @Test
    void givenNamespaceScopedAdapterActive_whenShatterVanillaBook_thenEnchantmentRemovedFromSource() {
        activateMockPlugin(new MockPluginAdapter("Vane"));
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        ItemStack sourceBook = enchantedBook("sharpness", "5", "unbreaking", "3");
        ItemStack resultBook = enchantedBook("sharpness", "5");

        InventoryClickEvent event = buildClickEvent(player, sourceBook, new ItemStack(Material.BOOK), resultBook);
        ShatterClickEvent.onEvent(event);

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastMockAnvil).setItem(eq(0), captor.capture());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) captor.getValue().getItemMeta();
        assertNotNull(meta);
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")),
                "Split enchantment must be removed from source book even when a namespace-scoped adapter is active (issue #73)");
        assertTrue(meta.hasStoredEnchant(enchantment("unbreaking")),
                "Non-split enchantment must remain on the source book");
    }

    // -> issue #76: an above-vanilla-max source level must survive to the delivered split book,
    //    even when the anvil result slot carries a clamped-down level (simulates the server
    //    clamping the level during the PrepareAnvilEvent → result-slot round-trip).

    @Test
    void givenResultSlotBookClampedBelowSourceLevel_whenClickResult_thenDeliveredBookKeepsRawLevel() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        // Source book holds Unbreaking 4 (above the vanilla max of 3); the split-off result book
        // has been clamped to 3, as a clamping server would leave it in slot 2.
        ItemStack sourceBook = enchantedBook("unbreaking", "4", "sharpness", "1");
        ItemStack resultBook = enchantedBook("unbreaking", "3");

        InventoryClickEvent event = buildClickEvent(player, sourceBook, new ItemStack(Material.BOOK), resultBook);
        ShatterClickEvent.onEvent(event);

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType(), "Result book must be placed on cursor");
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) cursor.getItemMeta();
        assertNotNull(meta, "Result book must have stored-enchant meta");
        assertEquals(4, meta.getStoredEnchantLevel(enchantment("unbreaking")),
                "Delivered book must carry the raw source level (4), not the clamped result-slot level (3) — issue #76");
    }

    // -> maintenance: the click on the (nulled) result slot of a genuine operation tells the player

    @Test
    void givenMaintenance_whenClickGenuineShatterSlot_thenPlayerMessagedAndNothingChanges() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            InventoryClickEvent event = buildClickEvent(player, enchantedBook("sharpness", "5", "unbreaking", "3"), new ItemStack(Material.BOOK), null);
            ShatterClickEvent.onEvent(event);

            assertNotNull(player.nextMessage(), "Player must be told maintenance is active");
            Mockito.verify(event).setCancelled(true);
            Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    @Test
    void givenMaintenance_whenClickVanillaAnvilResult_thenNoMessageAndNotCancelled() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            InventoryClickEvent event = buildClickEvent(player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND_SWORD));
            ShatterClickEvent.onEvent(event);

            assertNull(player.nextMessage(), "Vanilla anvil use must not trigger the maintenance message");
            Mockito.verify(event, Mockito.never()).setCancelled(true);
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    // -> economy: the charge and PreShatterEvent must cover only the split-off subset in the result
    //    book, not every eligible enchantment of the source book

    @Test
    void givenRandomSubsetInResult_whenShatter_thenEconomyAndPreEventCoverOnlySubset() {
        setConfig("shatterment.economy.enabled", true);
        setConfig("shatterment.anvil.repair.enchantment-costs.sharpness.economy", 10.0);
        setConfig("shatterment.anvil.repair.enchantment-costs.unbreaking.economy", 20.0);
        setConfig("shatterment.anvil.repair.enchantment-costs.mending.economy", 30.0);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            AtomicReference<PreShatterEvent> pre = new AtomicReference<>();
            AtomicReference<PostShatterEvent> post = new AtomicReference<>();
            server.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void on(PreShatterEvent e) {
                    pre.set(e);
                }

                @EventHandler
                public void on(PostShatterEvent e) {
                    post.set(e);
                }
            }, plugin);

            ItemStack sourceBook = enchantedBook("sharpness", "5", "unbreaking", "3", "mending", "1");
            // Prepare randomly picked only unbreaking for this split.
            InventoryClickEvent event = buildClickEvent(player, sourceBook, new ItemStack(Material.BOOK), enchantedBook("unbreaking", "3"));
            ShatterClickEvent.onEvent(event);

            assertNotNull(pre.get(), "PreShatterEvent must fire");
            assertEquals(List.of("unbreaking"), pre.get().getEnchantments().stream().map(IPluginEnchantment::getKey).toList(),
                    "PreShatterEvent must carry the split subset, not every eligible enchantment");
            Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(20.0));
            assertNotNull(post.get(), "PostShatterEvent must fire");
            assertEquals(20.0, post.get().getEconomyCost());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> economy robustness: a failed withdrawal must abort the operation

    @Test
    void givenWithdrawalFails_whenShatter_thenNothingHappensAndPlayerIsTold() {
        setConfig("shatterment.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenReturn(new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "bank limit"));
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            InventoryClickEvent event = buildClickEvent(player, enchantedBook("sharpness", "5", "unbreaking", "3"), new ItemStack(Material.BOOK), enchantedBook("sharpness", "5"));
            ShatterClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
            assertTrue(player.getItemOnCursor().getType().isAir(), "No result may be delivered when payment failed");
            assertEquals(10, player.getLevel(), "XP must not be deducted when payment failed");
            assertNotNull(player.nextMessage(), "Player must be told the payment failed");
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> economy robustness: abortable preconditions run before the player is charged

    @Test
    void givenResultWithoutItemMeta_whenShatter_thenPlayerIsNotCharged() {
        setConfig("shatterment.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            ItemStack metalessResult = Mockito.mock(ItemStack.class);
            Mockito.when(metalessResult.getType()).thenReturn(Material.ENCHANTED_BOOK);
            Mockito.when(metalessResult.getItemMeta()).thenReturn(null);

            InventoryClickEvent event = buildClickEvent(player, enchantedBook("sharpness", "5", "unbreaking", "3"), new ItemStack(Material.BOOK), metalessResult);
            ShatterClickEvent.onEvent(event);

            Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
            assertEquals(10, player.getLevel());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> a second operation is refused while the first one's blank-book removal is still pending

    @Test
    void givenPendingBookRemoval_whenSecondClickOnSameAnvil_thenRefusedUntilRemovalRan() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent first = buildClickEvent(player, enchantedBook("sharpness", "5", "unbreaking", "3"), new ItemStack(Material.BOOK, 2), enchantedBook("sharpness", "5"));
        ShatterClickEvent.onEvent(first);
        AnvilInventory anvil = lastMockAnvil;
        int levelAfterFirst = player.getLevel();
        assertTrue(AnvilEventGuards.isOperationPending(anvil), "First operation must leave the anvil pending");

        player.setItemOnCursor(null);
        InventoryClickEvent second = buildClickEvent(player, enchantedBook("sharpness", "5", "unbreaking", "3"), new ItemStack(Material.BOOK, 2), enchantedBook("sharpness", "5"));
        Mockito.when(second.getInventory()).thenReturn(anvil);
        ShatterClickEvent.onEvent(second);

        Mockito.verify(second).setCancelled(true);
        assertEquals(levelAfterFirst, player.getLevel(), "No second charge while pending");
        assertTrue(player.getItemOnCursor().getType().isAir(), "No second result while pending");

        server.getScheduler().performTicks(2);
        assertFalse(AnvilEventGuards.isOperationPending(anvil), "Pending clears after the deferred removal");
    }
}
