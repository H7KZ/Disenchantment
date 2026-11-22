package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.listeners.AnvilSessionListener;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

/**
 * "Too Expensive!" bypass and the insufficient-level feedback: the player is told how many levels
 * the real (cached) cost needs, and the cached cost does not outlive the anvil session.
 */
class BypassTooExpensiveTest extends DisenchantmentTestBase {

    private AnvilInventory lastMockAnvil;

    private InventoryClickEvent buildClickEvent(PlayerMock player, ItemStack slot0, ItemStack slot1, ItemStack slot2, int displayedCost) {
        AnvilInventory mockAnvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(mockAnvil.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(mockAnvil.getItem(0)).thenReturn(slot0);
        Mockito.when(mockAnvil.getItem(1)).thenReturn(slot1);
        Mockito.when(mockAnvil.getItem(2)).thenReturn(slot2);
        Mockito.when(mockAnvil.getRepairCost()).thenReturn(displayedCost);
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
        Mockito.when(event.getView()).thenReturn((InventoryView) viewProxy);
        return event;
    }

    private ItemStack sword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment("sharpness"), 5);
        return item;
    }

    private ItemStack resultBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("sharpness"), 5);
        return book;
    }

    // -> real cost above the player's level: the click is cancelled WITH feedback naming the real cost

    @Test
    void givenBypassedCostAbovePlayerLevel_whenClickResult_thenPlayerToldTheRealCost() {
        setConfig("disenchantment.anvil.repair.base", 50);
        setConfig("disenchantment.anvil.bypass-too-expensive", true);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(10);

        // Prepare caches the real cost (>= 40) and shows a cheap 1 level instead.
        PrepareAnvilEvent prepare = buildAnvilEvent(player, sword(), new ItemStack(Material.BOOK));
        DisenchantEvent.onEvent(prepare);

        InventoryClickEvent click = buildClickEvent(player, sword(), new ItemStack(Material.BOOK), resultBook(), 1);
        DisenchantClickEvent.onEvent(click);

        Mockito.verify(click).setCancelled(true);
        Mockito.verify(lastMockAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        String message = player.nextMessage();
        assertNotNull(message, "Player must be told why nothing happened");
        // base 50 + sharpness 5 * 0.25 multiplier = 51 levels (the displayed cost is only 1)
        assertTrue(message.contains("51"), "Message must name the real cost, got: " + message);
        assertEquals(10, player.getLevel());
    }

    @Test
    void givenInsufficientLevelsWithoutBypass_whenClickResult_thenPlayerToldTheCost() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(2);

        InventoryClickEvent click = buildClickEvent(player, sword(), new ItemStack(Material.BOOK), resultBook(), 7);
        DisenchantClickEvent.onEvent(click);

        Mockito.verify(click).setCancelled(true);
        String message = player.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("7"), "Message must name the required levels, got: " + message);
    }

    // -> cached bypass cost must not outlive the anvil session

    @Test
    void givenCachedBypassCost_whenPlayerQuits_thenCacheEntryRemoved() {
        setConfig("disenchantment.anvil.repair.base", 50);
        setConfig("disenchantment.anvil.bypass-too-expensive", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        DisenchantEvent.onEvent(buildAnvilEvent(player, sword(), new ItemStack(Material.BOOK)));
        assertTrue(AnvilEventGuards.peekBypassCost(player, 1) > 1, "Prepare must have cached the real cost");

        new AnvilSessionListener().onQuit(new PlayerQuitEvent(player, (net.kyori.adventure.text.Component) null,
                PlayerQuitEvent.QuitReason.DISCONNECTED));

        assertEquals(1, AnvilEventGuards.peekBypassCost(player, 1), "Cache entry must be dropped on quit");
    }

    @Test
    void givenCachedBypassCost_whenAnvilClosed_thenCacheEntryRemoved() {
        setConfig("disenchantment.anvil.repair.base", 50);
        setConfig("disenchantment.anvil.bypass-too-expensive", true);
        PlayerMock player = server.addPlayer("TestPlayer");

        DisenchantEvent.onEvent(buildAnvilEvent(player, sword(), new ItemStack(Material.BOOK)));
        assertTrue(AnvilEventGuards.peekBypassCost(player, 1) > 1);

        AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(anvil.getType()).thenReturn(InventoryType.ANVIL);
        InventoryCloseEvent close = Mockito.mock(InventoryCloseEvent.class);
        Mockito.when(close.getInventory()).thenReturn(anvil);
        Mockito.when(close.getPlayer()).thenReturn(player);
        new AnvilSessionListener().onClose(close);

        assertEquals(1, AnvilEventGuards.peekBypassCost(player, 1), "Cache entry must be dropped when the anvil closes");
    }
}
