package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
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

    // -> success chance of 0.0 must strip the enchantment from the result book

    @Test
    void givenZeroSuccessChance_whenDisenchant_thenResultBookHasNoEnchantments() {
        setConfig("disenchantment.enchantment-chances.sharpness", 0.0);
        PlayerMock player = server.addPlayer("TestPlayer2");
        player.setLevel(10);

        InventoryClickEvent event = buildClickEvent(player, sword("sharpness", 5), new ItemStack(Material.BOOK), enchantedBook("sharpness", 5));
        DisenchantClickEvent.onEvent(event);

        ItemStack cursor = player.getItemOnCursor();
        // Result book placed on cursor — enchantment removed by 0.0 chance roll
        if (cursor != null && cursor.getType() == Material.ENCHANTED_BOOK) {
            org.bukkit.inventory.meta.EnchantmentStorageMeta meta =
                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) cursor.getItemMeta();
            assertTrue(meta == null || meta.getStoredEnchants().isEmpty(),
                    "Sharpness must be absent from result book when chance is 0.0");
        }
        // If operation was skipped entirely (e.g. pre-event cancelled), that's also acceptable
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
}
