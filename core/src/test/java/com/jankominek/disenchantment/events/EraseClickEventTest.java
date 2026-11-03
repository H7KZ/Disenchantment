package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.api.PostEraseEvent;
import com.jankominek.disenchantment.events.api.PreEraseEvent;
import com.jankominek.disenchantment.utils.CooldownManager;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for EraseClickEvent — the manual-result pattern: the click is cancelled and the
 * slots and the cursor are mutated synchronously.
 */
class EraseClickEventTest extends DisenchantmentTestBase {

    private AnvilInventory lastAnvil;

    @BeforeEach
    void enableErase() {
        setConfig("erase.enabled", true);
    }

    private InventoryClickEvent buildClick(PlayerMock player, ItemStack slot0, ItemStack slot1, ItemStack slot2, int repairCost) {
        AnvilInventory anvil = Mockito.mock(AnvilInventory.class);
        Mockito.when(anvil.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(anvil.getItem(0)).thenReturn(slot0);
        Mockito.when(anvil.getItem(1)).thenReturn(slot1);
        Mockito.when(anvil.getItem(2)).thenReturn(slot2);
        Mockito.when(anvil.getRepairCost()).thenReturn(repairCost);
        Mockito.when(anvil.getViewers()).thenReturn(List.of(player));
        lastAnvil = anvil;

        List<Class<?>> interfaces = new ArrayList<>();
        interfaces.add(InventoryView.class);
        try {
            interfaces.add(Class.forName("org.bukkit.inventory.view.AnvilView"));
        } catch (ClassNotFoundException ignored) {
        }
        Object view = Proxy.newProxyInstance(getClass().getClassLoader(), interfaces.toArray(new Class[0]), (proxy, method, args) -> {
            if ("getPlayer".equals(method.getName())) return player;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType().isPrimitive()) return 0;
            return null;
        });

        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        Mockito.when(event.getWhoClicked()).thenReturn(player);
        Mockito.when(event.getInventory()).thenReturn(anvil);
        Mockito.when(event.getSlot()).thenReturn(2);
        Mockito.when(event.getRawSlot()).thenReturn(2);
        Mockito.when(event.getView()).thenReturn((InventoryView) view);
        return event;
    }

    private InventoryClickEvent click(PlayerMock player, ItemStack source, ItemStack catalyst, int repairCost) {
        ItemStack result = source.clone();
        result.getEnchantments().keySet().forEach(result::removeEnchantment);
        return buildClick(player, source, catalyst, result, repairCost);
    }

    private ItemStack sword(Object... keyLevelPairs) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        for (int i = 0; i < keyLevelPairs.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) keyLevelPairs[i]), (Integer) keyLevelPairs[i + 1]);
        }
        return item;
    }

    private ItemStack shard(int amount) {
        return new ItemStack(Material.ECHO_SHARD, amount);
    }

    private void assertNothingChanged(PlayerMock player, int level) {
        Mockito.verify(lastAnvil, Mockito.never()).setItem(Mockito.anyInt(), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir(), "Cursor must stay empty");
        assertEquals(level, player.getLevel(), "XP must not be charged");
    }

    // -> happy path

    @Test
    void givenValidErase_whenClick_thenCancelledAndResultOnCursorWithoutEnchantments() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5, "unbreaking", 3), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        ItemStack cursor = player.getItemOnCursor();
        assertEquals(Material.DIAMOND_SWORD, cursor.getType());
        assertTrue(cursor.getEnchantments().isEmpty());
    }

    @Test
    void givenValidErase_whenClick_thenSourceSlotClearedAndLastCatalystConsumed() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(1), 12));

        Mockito.verify(lastAnvil).setItem(0, null);
        Mockito.verify(lastAnvil).setItem(1, null);
        Mockito.verify(lastAnvil).setItem(2, null);
    }

    @Test
    void givenCatalystStack_whenClick_thenExactlyOneConsumed() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(5), 12));

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastAnvil).setItem(eq(1), captor.capture());
        assertEquals(Material.ECHO_SHARD, captor.getValue().getType());
        assertEquals(4, captor.getValue().getAmount());
    }

    @Test
    void givenConsumeCatalystOff_whenClick_thenCatalystKept() {
        setConfig("erase.consume-catalyst", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(3), 12));

        ArgumentCaptor<ItemStack> captor = ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(lastAnvil).setItem(eq(1), captor.capture());
        assertEquals(3, captor.getValue().getAmount());
        assertEquals(Material.DIAMOND_SWORD, player.getItemOnCursor().getType());
    }

    @Test
    void givenValidErase_whenClick_thenXpChargedFromTheDisplayedCost() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(1), 12));

        assertEquals(8, player.getLevel());
    }

    @Test
    void givenCurse_whenClick_thenCurseStaysOnDeliveredItem() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5, "vanishing_curse", 1), shard(1), 12));

        ItemStack cursor = player.getItemOnCursor();
        assertFalse(cursor.containsEnchantment(enchantment("sharpness")));
        assertTrue(cursor.containsEnchantment(enchantment("vanishing_curse")));
    }

    @Test
    void givenCreativePlayer_whenClick_thenNoXpChargedButCatalystConsumed() {
        PlayerMock player = server.addPlayer("Creative");
        player.setGameMode(GameMode.CREATIVE);
        player.setLevel(3);

        AtomicReference<PostEraseEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostEraseEvent e) {
                captured.set(e);
            }
        }, plugin);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(1), 12));

        assertEquals(3, player.getLevel());
        Mockito.verify(lastAnvil).setItem(1, null);
        assertEquals(0, captured.get().getXpCost());
        assertEquals(0.0, captured.get().getEconomyCost());
    }

    // -> API events

    @Test
    void givenValidErase_whenClick_thenPostEventCarriesResultErasedAndCosts() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        AtomicReference<PostEraseEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostEraseEvent e) {
                captured.set(e);
            }
        }, plugin);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5, "unbreaking", 3), shard(1), 12));

        PostEraseEvent post = captured.get();
        assertNotNull(post, "PostEraseEvent must fire");
        assertEquals(12, post.getXpCost());
        assertEquals(2, post.getErased().size());
        assertTrue(post.getResultItem().getEnchantments().isEmpty());
    }

    @Test
    void givenPreEventCarriesSnapshots_whenClick_thenSourceCatalystAndEnchantmentsExposed() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        AtomicReference<PreEraseEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreEraseEvent e) {
                captured.set(e);
            }
        }, plugin);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(2), 12));

        assertEquals(Material.DIAMOND_SWORD, captured.get().getSourceItem().getType());
        assertEquals(Material.ECHO_SHARD, captured.get().getCatalyst().getType());
        assertEquals(2, captured.get().getCatalyst().getAmount());
        assertEquals(1, captured.get().getEnchantments().size());
    }

    @Test
    void givenListenerCancelsPreEvent_whenClick_thenNothingChanges() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreEraseEvent e) {
                e.setCancelled(true);
            }
        }, plugin);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNothingChanged(player, 20);
    }

    @Test
    void givenListenerVetoesOneEnchantment_whenClick_thenItStaysOnTheItem() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreEraseEvent e) {
                e.getEnchantments().removeIf(ench -> ench.getKey().equals("unbreaking"));
            }
        }, plugin);

        EraseClickEvent.onEvent(click(player, sword("sharpness", 5, "unbreaking", 3), shard(1), 12));

        ItemStack cursor = player.getItemOnCursor();
        assertFalse(cursor.containsEnchantment(enchantment("sharpness")));
        assertTrue(cursor.containsEnchantment(enchantment("unbreaking")));
    }

    // -> refusals leave everything untouched (no dupe, no loss)

    @Test
    void givenNotEnoughLevels_whenClick_thenCancelledNothingChangedAndPlayerTold() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(5);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNothingChanged(player, 5);
        assertNotNull(player.nextMessage());
        assertEquals(1, player.getHeardSounds().size(), "Failure sound");
    }

    @Test
    void givenShiftClick_whenClick_thenCancelledAndNothingConsumed() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        Mockito.when(event.getClick()).thenReturn(ClickType.SHIFT_LEFT);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNothingChanged(player, 20);
    }

    @Test
    void givenNumberKeyClick_whenClick_thenCancelledAndNothingConsumed() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        Mockito.when(event.getClick()).thenReturn(ClickType.NUMBER_KEY);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNothingChanged(player, 20);
    }

    @Test
    void givenCooldown_whenClick_thenCancelledAndNothingChanges() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        CooldownManager.recordOperation(player);
        try {
            InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
            EraseClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            assertNothingChanged(player, 20);
        } finally {
            CooldownManager.reset();
        }
    }

    @Test
    void givenSuccessfulErase_whenClick_thenCooldownStarted() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        try {
            EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(1), 12));

            assertTrue(CooldownManager.isOnCooldown(player));
        } finally {
            CooldownManager.reset();
        }
    }

    @Test
    void givenMaintenance_whenClick_thenCancelledAndNothingChanges() {
        Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);

            InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
            EraseClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            assertNothingChanged(player, 20);
        } finally {
            Disenchantment.maintenanceEnabled = false;
        }
    }

    @Test
    void givenNoPermission_whenClick_thenCancelledAndNothingChanges() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        player.addAttachment(plugin, "disenchantment.anvil.erase", false);
        player.addAttachment(plugin, "disenchantment.anvil.all", false);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        assertNothingChanged(player, 20);
    }

    // -> not an erase: stay out of the way

    @Test
    void givenEraseDisabled_whenClick_thenIgnored() {
        setConfig("erase.enabled", false);
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        assertNothingChanged(player, 20);
    }

    @Test
    void givenVanillaRepair_whenClick_thenIgnored() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = buildClick(player, sword("sharpness", 5), new ItemStack(Material.DIAMOND), sword("sharpness", 5), 3);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        assertNothingChanged(player, 20);
    }

    @Test
    void givenSlotOutsideAnvil_whenClick_thenIgnored() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        Mockito.when(event.getRawSlot()).thenReturn(32);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        assertNothingChanged(player, 20);
    }

    @Test
    void givenCursorNotEmpty_whenClick_thenIgnoredSoNothingIsOverwritten() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        player.setItemOnCursor(new ItemStack(Material.DIRT));

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(lastAnvil, Mockito.never()).setItem(Mockito.anyInt(), Mockito.any());
        assertEquals(Material.DIRT, player.getItemOnCursor().getType());
    }

    @Test
    void givenDisableState_whenClick_thenIgnored() {
        setDisenchantEnchantmentStates(List.of("unbreaking:disable"));
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5, "unbreaking", 3), shard(1), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        assertNothingChanged(player, 20);
    }

    @Test
    void givenDisenchantWithBook_whenEraseClick_thenIgnored() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        InventoryClickEvent event = buildClick(player, sword("sharpness", 5), new ItemStack(Material.BOOK), book, 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        assertNothingChanged(player, 20);
    }

    // -> economy

    @Test
    void givenEconomyEnabled_whenClick_thenChargedFromEraseEconomyBlock() {
        setConfig("erase.economy.enabled", true);
        setConfig("erase.economy.cost", 75.0);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);
            AtomicReference<PostEraseEvent> captured = new AtomicReference<>();
            server.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void on(PostEraseEvent e) {
                    captured.set(e);
                }
            }, plugin);

            EraseClickEvent.onEvent(click(player, sword("sharpness", 5), shard(1), 12));

            Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), eq(75.0));
            assertEquals(75.0, captured.get().getEconomyCost());
            assertEquals(Material.DIAMOND_SWORD, player.getItemOnCursor().getType());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenWithdrawalFails_whenClick_thenNothingChanges() {
        setConfig("erase.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenReturn(new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "bank limit"));
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);

            InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
            EraseClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            assertNothingChanged(player, 20);
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenInsufficientFunds_whenClick_thenNothingChanges() {
        setConfig("erase.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.has(Mockito.any(OfflinePlayer.class), Mockito.anyDouble())).thenReturn(false);
        try {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.setLevel(20);

            InventoryClickEvent event = click(player, sword("sharpness", 5), shard(1), 12);
            EraseClickEvent.onEvent(event);

            Mockito.verify(event).setCancelled(true);
            assertNothingChanged(player, 20);
            Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    // -> special anvils: the gate fails closed at click time

    @Test
    void givenMarkedModeAndVirtualAnvil_whenClick_thenCancelledResultClearedInputsUntouched() {
        setConfig("special-anvils.mode", "MARKED");
        PlayerMock player = server.addPlayer("TestPlayer");
        player.setLevel(20);

        InventoryClickEvent event = click(player, sword("sharpness", 5), shard(3), 12);
        EraseClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastAnvil).setItem(2, null);
        Mockito.verify(lastAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        Mockito.verify(lastAnvil, Mockito.never()).setItem(eq(1), Mockito.any());
        assertEquals(20, player.getLevel());
    }
}
