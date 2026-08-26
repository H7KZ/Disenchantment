package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
