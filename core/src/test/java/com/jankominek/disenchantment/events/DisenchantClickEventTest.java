package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.utils.CooldownManager;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Integration tests for DisenchantClickEvent — simulates a player clicking the
 * anvil result slot and asserts the handler correctly modifies the source item
 * and delivers the result book.
 */
class DisenchantClickEventTest extends DisenchantmentTestBase {

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

    private ItemStack sword(String enchantKey, int level) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment(enchantKey), level);
        return item;
    }

    private ItemStack enchantedBook(String enchantKey, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment(enchantKey), level);
        return book;
    }

    // -> B3: null secondItem must abort before slot 0 is touched

    @Test
    void givenNullSecondSlot_whenClickResult_thenSlot0NotModified() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), null, enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
    }

    // -> cancelling PreDisenchantEvent aborts the operation

    @Test
    void givenListenerCancelsPreEvent_whenClickResult_thenSlot0NotModified() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreDisenchantEvent e) {
                e.setCancelled(true);
            }
        }, plugin);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir(), "Cursor must stay empty when operation is cancelled");
    }

    // -> getSlot() == 2 on the player's own inventory (third hotbar slot) is not the result slot

    @Test
    void givenClickOnPlayerInventorySlotTwo_whenClickResult_thenNoOperation() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(event.getRawSlot()).thenReturn(32);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir(), "Cursor must stay empty for a non-result-slot click");
        assertEquals(10, player.getLevel(), "XP must not be charged for a non-result-slot click");
    }

    // -> cooldown must not block vanilla anvil results (repair/rename)

    @Test
    void givenPlayerOnCooldown_whenClickVanillaAnvilResult_thenClickNotCancelled() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        CooldownManager.recordOperation(player);

        try {
            InventoryClickEvent event = buildClickEvent(player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND_SWORD));
            DisenchantClickEvent.onEvent(event);

            Mockito.verify(event, Mockito.never()).setCancelled(true);
        } finally {
            CooldownManager.reset();
        }
    }

    // -> cooldown still blocks a genuine disenchant

    @Test
    void givenPlayerOnCooldown_whenClickDisenchantResult_thenCancelled() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        CooldownManager.recordOperation(player);

        try {
            InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
            DisenchantClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
            assertNotNull(player.nextMessage(), "The click path tells the player about the cooldown");
        } finally {
            CooldownManager.reset();
        }
    }

    // -> happy path: enchantment removed from source item

    @Test
    void givenValidDisenchant_whenClickResult_thenEnchantmentRemovedFromSource() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        Enchantment sharpness = enchantment("sharpness");

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastMockAnvil).setItem(eq(0), captor.capture());
        assertFalse(captor.getValue().containsEnchantment(sharpness), "Sharpness must be removed from source item");
    }

    // -> happy path: result book lands on cursor

    @Test
    void givenValidDisenchant_whenClickResult_thenBookPlacedOnCursor() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType(), "Result book must be placed on cursor");
    }

    // -> issue #73: with a namespace-scoped adapter (Vane) active, a vanilla enchantment
    //    must still be stripped from the source item. Otherwise the book is delivered while
    //    the item keeps its enchantment — a dupe.

    @Test
    void givenNamespaceScopedAdapterActive_whenDisenchantVanillaItem_thenEnchantmentRemovedFromSource() {
        activateMockPlugin(new MockPluginAdapter("Vane"));
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        Enchantment sharpness = enchantment("sharpness");

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastMockAnvil).setItem(eq(0), captor.capture());
        assertFalse(captor.getValue().containsEnchantment(sharpness),
                "Sharpness must be removed from source item even when a namespace-scoped adapter is active (issue #73)");
    }

    // -> plugin disabled

    @Test
    void givenPluginDisabled_whenClickResult_thenNoChanges() {
        setConfig("enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
    }

    // -> Creative-mode: PostDisenchantEvent must report xpCost=0 and economyCost=0

    @Test
    void givenCreativePlayer_whenDisenchant_thenPostEventReportsZeroCosts() {
        PlayerMock player = server.addPlayer("CreativePlayer");
        player.setLevel(10);
        player.setGameMode(GameMode.CREATIVE);

        AtomicReference<PostDisenchantEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostDisenchantEvent e) {
                captured.set(e);
            }
        }, plugin);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        assertNotNull(captured.get(), "PostDisenchantEvent must fire");
        assertEquals(0, captured.get().getXpCost(), "Creative players must report xpCost=0");
        assertEquals(0.0, captured.get().getEconomyCost(), "Creative players must report economyCost=0");
    }

    // -> success chance of 0.0 on every enchantment: the player gets a plain book and a message,
    //    never an empty enchanted book

    @Test
    void givenZeroSuccessChance_whenDisenchant_thenPlainBookDeliveredWithMessage() {
        setConfig("disenchantment.enchantment-chances.sharpness", 0.0);
        PlayerMock player = server.addPlayer("TestPlayer2");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.BOOK, cursor.getType(), "A plain book must replace the empty enchanted book");
        assertNotNull(player.nextMessage(), "Player must be told the transfer failed");

        // The enchantment is still gone from the source (it was destroyed by the failed roll).
        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastMockAnvil).setItem(eq(0), captor.capture());
        assertFalse(captor.getValue().containsEnchantment(enchantment("sharpness")));
    }

    @Test
    void givenZeroChanceOnOnlyOneOfTwoEnchantments_whenDisenchant_thenEnchantedBookWithSurvivorDelivered() {
        setConfig("disenchantment.enchantment-chances.sharpness", 0.0);
        PlayerMock player = server.addPlayer("TestPlayer2");
        player.setLevel(10);

        ItemStack sword = sword("sharpness", 5);
        sword.addUnsafeEnchantment(enchantment("unbreaking"), 3);
        ItemStack book = enchantedBook("sharpness", 5);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("unbreaking"), 3);

        InventoryClickEvent event = buildClickEvent(player, sword, new ItemStack(Material.BOOK), book);
        DisenchantClickEvent.onEvent(event);

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType());
        org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) cursor.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.hasStoredEnchant(enchantment("unbreaking")));
        assertFalse(meta.hasStoredEnchant(enchantment("sharpness")));
        assertNull(player.nextMessage(), "No failure message when something transferred");
    }

    // -> issue #76: an above-vanilla-max source level must survive to the delivered book, even
    //    when the anvil result slot carries a clamped-down level (simulates the server clamping
    //    the level during the PrepareAnvilEvent → result-slot round-trip).

    @Test
    void givenResultSlotBookClampedBelowSourceLevel_whenClickResult_thenDeliveredBookKeepsRawLevel() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        Enchantment unbreaking = enchantment("unbreaking");

        // Source item is Unbreaking 4 (above the vanilla max of 3); result slot holds a book whose
        // level has been clamped to 3, as a clamping server would leave it in slot 2.
        InventoryClickEvent event = buildClickEvent(player, sword("unbreaking", 4), new ItemStack(Material.BOOK), enchantedBook("unbreaking", 3));
        DisenchantClickEvent.onEvent(event);

        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, cursor.getType(), "Result book must be placed on cursor");
        org.bukkit.inventory.meta.EnchantmentStorageMeta meta =
                (org.bukkit.inventory.meta.EnchantmentStorageMeta) cursor.getItemMeta();
        assertNotNull(meta, "Result book must have stored-enchant meta");
        assertEquals(4, meta.getStoredEnchantLevel(unbreaking),
                "Delivered book must carry the raw source level (4), not the clamped result-slot level (3) — issue #76");
    }

    // -> PreDisenchantEvent enchantment list: removing from list prevents that DELETE from running

    @Test
    void givenListenerRemovesDeleteEnchantmentFromPreEvent_thenEnchantmentNotDeletedFromSource() {
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        sword.addUnsafeEnchantment(enchantment("mending"), 1);

        // Result book only has sharpness (mending was in DELETE state, not transferred)
        ItemStack book = enchantedBook("sharpness", 5);

        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreDisenchantEvent e) {
                e.getEnchantments().removeIf(enc -> enc.getKey().equals("mending"));
            }
        }, plugin);

        InventoryClickEvent event = buildClickEvent(player, sword, new ItemStack(Material.BOOK), book);
        DisenchantClickEvent.onEvent(event);

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastMockAnvil).setItem(eq(0), captor.capture());
        assertTrue(captor.getValue().containsEnchantment(enchantment("mending")),
                "Mending must remain on source when removed from PreDisenchantEvent list");
    }

    // -> maintenance: the click on the (nulled) result slot of a genuine operation tells the player

    @Test
    void givenMaintenance_whenClickGenuineDisenchantSlot_thenPlayerMessagedAndNothingChanges() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            // Prepare nulled the result, so slot 2 is empty.
            InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), null);
            DisenchantClickEvent.onEvent(event);

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
            DisenchantClickEvent.onEvent(event);

            assertNull(player.nextMessage(), "Vanilla anvil use must not trigger the maintenance message");
            Mockito.verify(event, Mockito.never()).setCancelled(true);
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    // -> economy: the charge must cover exactly the enchantments transferred to the result book
    //    (what the Prepare action bar showed), not DELETE-state enchantments that are only destroyed

    @Test
    void givenDeleteStateEnchantment_whenDisenchant_thenEconomyChargesOnlyTransferredEnchantments() {
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        setConfig("disenchantment.economy.enabled", true);
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness.economy", 10.0);
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.economy", 50.0);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            ItemStack sword = sword("sharpness", 5);
            sword.addUnsafeEnchantment(enchantment("mending"), 1);

            AtomicReference<PostDisenchantEvent> captured = new AtomicReference<>();
            server.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void on(PostDisenchantEvent e) {
                    captured.set(e);
                }
            }, plugin);

            // Prepare puts only sharpness on the book (mending is DELETE-state) and displays 10.0.
            InventoryClickEvent event = buildClickEvent(player, sword, new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
            DisenchantClickEvent.onEvent(event);

            Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(10.0));
            Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(60.0));
            assertNotNull(captured.get(), "PostDisenchantEvent must fire");
            assertEquals(10.0, captured.get().getEconomyCost(), "Reported economy cost must match the charge");

            // DELETE behaviour is unchanged: mending is still stripped from the source item.
            ArgumentCaptor<ItemStack> source = ArgumentCaptor.forClass(ItemStack.class);
            Mockito.verify(lastMockAnvil).setItem(eq(0), source.capture());
            assertFalse(source.getValue().containsEnchantment(enchantment("mending")), "DELETE-state enchantment must still be removed");
            assertFalse(source.getValue().containsEnchantment(enchantment("sharpness")));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> economy robustness: a failed withdrawal must abort the operation

    @Test
    void givenWithdrawalFails_whenDisenchant_thenNothingHappensAndPlayerIsTold() {
        setConfig("disenchantment.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenReturn(new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "bank limit"));
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
            DisenchantClickEvent.onEvent(event);

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
    void givenResultWithoutItemMeta_whenDisenchant_thenPlayerIsNotCharged() {
        setConfig("disenchantment.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(10);

            ItemStack metalessResult = Mockito.mock(ItemStack.class);
            Mockito.when(metalessResult.getType()).thenReturn(Material.ENCHANTED_BOOK);
            Mockito.when(metalessResult.getItemMeta()).thenReturn(null);

            InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), metalessResult);
            DisenchantClickEvent.onEvent(event);

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

        InventoryClickEvent first = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK, 2), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(first);
        AnvilInventory anvil = lastMockAnvil;
        int levelAfterFirst = player.getLevel();
        assertTrue(AnvilEventGuards.isOperationPending(anvil), "First operation must leave the anvil pending");

        // Second click in the same tick (cursor emptied, e.g. by a crafted packet sequence).
        player.setItemOnCursor(null);
        InventoryClickEvent second = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK, 2), enchantedBook("sharpness", 5));
        Mockito.when(second.getInventory()).thenReturn(anvil);
        DisenchantClickEvent.onEvent(second);

        Mockito.verify(second).setCancelled(true);
        assertEquals(levelAfterFirst, player.getLevel(), "No second charge while pending");
        assertTrue(player.getItemOnCursor().getType().isAir(), "No second result while pending");

        server.getScheduler().performTicks(2);
        assertFalse(AnvilEventGuards.isOperationPending(anvil), "Pending clears after the deferred removal");
    }

    // -> unsafe click types are cancelled WITH feedback

    @Test
    void givenShiftClickOnGenuineDisenchant_whenClickResult_thenCancelledAndPlayerTold() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(event.getClick()).thenReturn(org.bukkit.event.inventory.ClickType.SHIFT_LEFT);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        assertNotNull(player.nextMessage(), "Player must be told to take the result with a normal click");
    }

    @Test
    void givenShiftClickOnVanillaAnvilResult_whenClickResult_thenNeitherCancelledNorMessaged() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND_SWORD));
        Mockito.when(event.getClick()).thenReturn(org.bukkit.event.inventory.ClickType.SHIFT_LEFT);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(true);
        assertNull(player.nextMessage());
    }

    // -> feedback: refused clicks play the failure sound, the message is debounced

    @Test
    void givenNotEnoughLevels_whenClickResult_thenCancelledWithFailureSoundAndOneDebouncedMessage() {
        com.jankominek.disenchantment.utils.FeedbackUtils.reset();
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(5);

        InventoryClickEvent first = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(30);
        DisenchantClickEvent.onEvent(first);
        InventoryClickEvent second = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(30);
        DisenchantClickEvent.onEvent(second);

        Mockito.verify(first).setCancelled(true);
        Mockito.verify(second).setCancelled(true);
        assertEquals(2, player.getHeardSounds().size(), "Every refused click plays the failure sound");
        assertNotNull(player.nextMessage(), "The first refusal is explained");
        assertNull(player.nextMessage(), "The immediate repeat is debounced");
        assertEquals(5, player.getLevel());
    }

    @Test
    void givenNotEnoughLevelsAndMessageDisabled_whenClickResult_thenSilentExceptSound() {
        com.jankominek.disenchantment.utils.FeedbackUtils.reset();
        setConfig("feedback.not-enough-xp-message", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(5);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(30);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNull(player.nextMessage());
        assertEquals(1, player.getHeardSounds().size());
    }

    @Test
    void givenShiftClickOnGenuineDisenchant_whenClickResult_thenFailureSoundPlayed() {
        com.jankominek.disenchantment.utils.FeedbackUtils.reset();
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(event.getClick()).thenReturn(org.bukkit.event.inventory.ClickType.SHIFT_LEFT);
        DisenchantClickEvent.onEvent(event);

        assertEquals(1, player.getHeardSounds().size());
    }

    @Test
    void givenFailureSoundDisabled_whenRefusedClick_thenNoSound() {
        com.jankominek.disenchantment.utils.FeedbackUtils.reset();
        setConfig("feedback.failure-sound.enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(5);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(30);
        DisenchantClickEvent.onEvent(event);

        assertTrue(player.getHeardSounds().isEmpty());
    }
    // -> Too Expensive!: the handler delivers ahead of vanilla's own pick-up check, so it enforces the 40 cap itself

    @Test
    void givenCostOver40AndBypassOff_whenClick_thenRefusedAndNothingChanges() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(60);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(45);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        assertEquals(60, player.getLevel());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertNotNull(player.nextMessage());
    }

    @Test
    void givenCostOver40AndBypassOn_whenClick_thenDelivered() {
        setConfig("disenchantment.anvil.bypass-too-expensive", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(60);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(lastMockAnvil.getRepairCost()).thenReturn(45);
        DisenchantClickEvent.onEvent(event);

        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType());
    }

    // -> an earlier veto (protection/shop plugin) wins

    @Test
    void givenClickAlreadyCancelled_whenClick_thenNothingIsChargedOrMoved() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        Mockito.when(event.isCancelled()).thenReturn(true);
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(Mockito.anyInt(), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(10, player.getLevel());
    }

    @Test
    void givenCursorNotEmpty_whenClick_thenIgnoredBecauseBooksCannotStack() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);
        player.setItemOnCursor(new ItemStack(Material.ENCHANTED_BOOK));

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(Mockito.anyInt(), Mockito.any());
        assertEquals(10, player.getLevel());
    }
}
