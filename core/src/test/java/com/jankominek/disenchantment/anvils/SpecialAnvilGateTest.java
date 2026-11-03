package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.AnvilEventGuards;
import com.jankominek.disenchantment.events.DisenchantClickEvent;
import com.jankominek.disenchantment.events.DisenchantEvent;
import com.jankominek.disenchantment.events.ShatterClickEvent;
import com.jankominek.disenchantment.events.ShatterEvent;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests the special anvil gate ({@link AnvilEventGuards#isAnvilAllowed}) and its effect on the
 * disenchant and shatter Prepare/Click handlers for every {@code special-anvils.mode}. Vanilla anvil
 * use must stay untouched: a refused anvil never gets a plugin result and never cancels a click.
 */
class SpecialAnvilGateTest extends DisenchantmentTestBase {

    private Block anvil;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        anvil = world().getBlockAt(5, 64, 5);
        anvil.setType(Material.ANVIL);
        player = server.addPlayer("Smith");
        player.setLevel(30);
    }

    // -> helpers

    private AnvilInventory anvilInventory(Location location) {
        AnvilInventory inv = Mockito.mock(AnvilInventory.class);
        Mockito.when(inv.getLocation()).thenReturn(location);
        return inv;
    }

    private Location anvilLocation() {
        return anvil.getLocation();
    }

    private ItemStack sword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment("sharpness"), 5);
        return item;
    }

    private ItemStack enchantedBook(String... keys) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        for (String key : keys) EnchantmentUtils.addStoredEnchantment(book, enchantment(key), 3);
        return book;
    }

    private PrepareAnvilEvent prepare(ItemStack slot0, ItemStack slot1, Location location) {
        PrepareAnvilEvent event = buildAnvilEvent(player, slot0, slot1);
        Mockito.when(event.getInventory().getLocation()).thenReturn(location);
        return event;
    }

    private AnvilInventory lastClickAnvil;

    private InventoryClickEvent click(ItemStack slot0, ItemStack slot1, ItemStack slot2, Location location) {
        AnvilInventory inv = anvilInventory(location);
        Mockito.when(inv.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(inv.getItem(0)).thenReturn(slot0);
        Mockito.when(inv.getItem(1)).thenReturn(slot1);
        Mockito.when(inv.getItem(2)).thenReturn(slot2);
        Mockito.when(inv.getRepairCost()).thenReturn(0);
        Mockito.when(inv.getViewers()).thenReturn(List.of(player));
        lastClickAnvil = inv;

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
        Mockito.when(event.getInventory()).thenReturn(inv);
        Mockito.when(event.getSlot()).thenReturn(2);
        Mockito.when(event.getRawSlot()).thenReturn(2);
        Mockito.when(event.getView()).thenReturn((InventoryView) view);
        return event;
    }

    // -> gate: ALL

    @Test
    void givenAllMode_whenCheckedAnywhere_thenAllowed() {
        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(null), player), "virtual anvils work in ALL mode");
        assertTrue(AnvilEventGuards.isAnvilAllowed(null, player));
    }

    // -> gate: MARKED

    @Test
    void givenMarkedMode_whenAnvilMarked_thenAllowed() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);

        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenMarkedMode_whenAnvilNotMarked_thenRefused() {
        setConfig("special-anvils.mode", "MARKED");

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenMarkedMode_whenMarkIsStale_thenRefused() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        anvil.setType(Material.STONE);

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenMarkedMode_whenVirtualAnvil_thenFollowsAllowVirtualSetting() {
        setConfig("special-anvils.mode", "MARKED");

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(null), player));

        setConfig("special-anvils.allow-virtual-anvils", true);

        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(null), player));
    }

    @Test
    void givenMarkedMode_whenLocationIsNotAnAnvilBlock_thenTreatedAsVirtual() {
        setConfig("special-anvils.mode", "MARKED");
        Block stone = world().getBlockAt(9, 64, 9);
        stone.setType(Material.STONE);

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(stone.getLocation()), player));

        setConfig("special-anvils.allow-virtual-anvils", true);

        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(stone.getLocation()), player));
    }

    // -> gate: BLOCK_BELOW

    @Test
    void givenBlockBelowMode_whenStandingOnConfiguredBlock_thenAllowed() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        anvil.getRelative(0, -1, 0).setType(Material.LAPIS_BLOCK);

        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenBlockBelowMode_whenStandingOnOtherBlock_thenRefused() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        anvil.getRelative(0, -1, 0).setType(Material.STONE);

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenBlockBelowModeWithCustomBlock_whenChecked_thenUsesConfiguredBlock() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        setConfig("special-anvils.block-below", "emerald_block");
        anvil.getRelative(0, -1, 0).setType(Material.LAPIS_BLOCK);

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));

        anvil.getRelative(0, -1, 0).setType(Material.EMERALD_BLOCK);

        assertTrue(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    @Test
    void givenBlockBelowMode_whenAnvilMarkedButNotOnBlock_thenMarkIsIrrelevant() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        SpecialAnvilStore.mark(anvil);

        assertFalse(AnvilEventGuards.isAnvilAllowed(anvilInventory(anvilLocation()), player));
    }

    // -> disenchant prepare

    @Test
    void givenMarkedMode_whenDisenchantPrepareAtUnmarkedAnvil_thenNoPluginResult() {
        setConfig("special-anvils.mode", "MARKED");
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());

        DisenchantEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
    }

    @Test
    void givenMarkedMode_whenDisenchantPrepareAtMarkedAnvil_thenBookResult() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());

        DisenchantEvent.onEvent(event);

        assertNotNull(event.getResult());
        assertEquals(Material.ENCHANTED_BOOK, event.getResult().getType());
    }

    @Test
    void givenAllMode_whenDisenchantPrepareAtVirtualAnvil_thenBookResult() {
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), null);

        DisenchantEvent.onEvent(event);

        assertNotNull(event.getResult());
    }

    @Test
    void givenBlockBelowMode_whenDisenchantPrepare_thenOnlyOnConfiguredBlock() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        PrepareAnvilEvent refused = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());
        DisenchantEvent.onEvent(refused);
        Mockito.verify(refused, Mockito.never()).setResult(Mockito.any());

        anvil.getRelative(0, -1, 0).setType(Material.LAPIS_BLOCK);
        PrepareAnvilEvent accepted = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());
        DisenchantEvent.onEvent(accepted);

        assertNotNull(accepted.getResult());
    }

    // -> shatter prepare

    @Test
    void givenMarkedMode_whenShatterPrepareAtUnmarkedAnvil_thenNoPluginResult() {
        setConfig("special-anvils.mode", "MARKED");
        PrepareAnvilEvent event = prepare(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK), anvilLocation());

        ShatterEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
    }

    @Test
    void givenMarkedMode_whenShatterPrepareAtMarkedAnvil_thenBookResult() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        PrepareAnvilEvent event = prepare(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK), anvilLocation());

        ShatterEvent.onEvent(event);

        assertNotNull(event.getResult());
    }

    // -> vanilla anvil use is untouched

    @Test
    void givenMarkedMode_whenVanillaRepairPrepareAtAnyAnvil_thenNeverTouched() {
        setConfig("special-anvils.mode", "MARKED");
        setConfig("special-anvils.hint-message", true);
        PrepareAnvilEvent event = prepare(new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND), anvilLocation());

        DisenchantEvent.onEvent(event);
        ShatterEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
        assertNull(player.nextMessage(), "no hint for a vanilla operation");
    }

    @Test
    void givenMarkedMode_whenVanillaClickAtUnmarkedAnvil_thenNotCancelledNorModified() {
        setConfig("special-anvils.mode", "MARKED");
        InventoryClickEvent event = click(new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND),
                new ItemStack(Material.DIAMOND_SWORD), anvilLocation());

        DisenchantClickEvent.onEvent(event);
        ShatterClickEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setCancelled(true);
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(Mockito.anyInt(), Mockito.any());
    }

    // -> disenchant / shatter click

    @Test
    void givenMarkedMode_whenDisenchantClickAtUnmarkedAnvil_thenRefusedAndNothingChanges() {
        setConfig("special-anvils.mode", "MARKED");
        ItemStack result = enchantedBook("sharpness");
        InventoryClickEvent event = click(sword(), new ItemStack(Material.BOOK), result, anvilLocation());

        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(1), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(30, player.getLevel(), "no XP charged");
    }

    @Test
    void givenGateOpenAtPrepareButClosedAtClick_whenDisenchantClick_thenCancelledAndResultCleared() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        PrepareAnvilEvent prepared = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());
        DisenchantEvent.onEvent(prepared);
        ItemStack pluginResult = prepared.getResult();
        assertNotNull(pluginResult, "Prepare produced the plugin book while the anvil was special");

        SpecialAnvilStore.unmark(anvil);
        InventoryClickEvent event = click(sword(), new ItemStack(Material.BOOK), pluginResult, anvilLocation());
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastClickAnvil).setItem(2, null);
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(1), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(30, player.getLevel(), "no XP charged");
    }

    @Test
    void givenBlockBelowRemovedAfterPrepare_whenDisenchantClick_thenCancelledAndResultCleared() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        anvil.getRelative(0, -1, 0).setType(Material.LAPIS_BLOCK);
        PrepareAnvilEvent prepared = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());
        DisenchantEvent.onEvent(prepared);
        ItemStack pluginResult = prepared.getResult();
        assertNotNull(pluginResult);

        anvil.getRelative(0, -1, 0).setType(Material.STONE);
        InventoryClickEvent event = click(sword(), new ItemStack(Material.BOOK), pluginResult, anvilLocation());
        DisenchantClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastClickAnvil).setItem(2, null);
    }

    @Test
    void givenGateOpenAtPrepareButClosedAtClick_whenShatterClick_thenCancelledAndResultCleared() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        PrepareAnvilEvent prepared = prepare(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK), anvilLocation());
        ShatterEvent.onEvent(prepared);
        ItemStack pluginResult = prepared.getResult();
        assertNotNull(pluginResult, "Prepare produced the plugin book while the anvil was special");

        SpecialAnvilStore.unmark(anvil);
        InventoryClickEvent event = click(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK), pluginResult, anvilLocation());
        ShatterClickEvent.onEvent(event);

        Mockito.verify(event).setCancelled(true);
        Mockito.verify(lastClickAnvil).setItem(2, null);
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(1), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(30, player.getLevel());
    }

    @Test
    void givenMarkedMode_whenDisenchantClickAtMarkedAnvil_thenOperationRuns() {
        setConfig("special-anvils.mode", "MARKED");
        SpecialAnvilStore.mark(anvil);
        InventoryClickEvent event = click(sword(), new ItemStack(Material.BOOK), enchantedBook("sharpness"), anvilLocation());

        DisenchantClickEvent.onEvent(event);

        Mockito.verify(lastClickAnvil).setItem(eq(0), Mockito.any());
        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType());
    }

    @Test
    void givenMarkedMode_whenShatterClickAtUnmarkedAnvil_thenRefusedAndNothingChanges() {
        setConfig("special-anvils.mode", "MARKED");
        InventoryClickEvent event = click(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK),
                enchantedBook("sharpness"), anvilLocation());

        ShatterClickEvent.onEvent(event);

        Mockito.verify(lastClickAnvil, Mockito.never()).setItem(eq(0), Mockito.any());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(30, player.getLevel());
    }

    // -> hint message

    @Test
    void givenHintEnabled_whenDisenchantPrepareAtRefusedAnvil_thenHintSentOnceAndNoResult() {
        setConfig("special-anvils.mode", "MARKED");
        setConfig("special-anvils.hint-message", true);
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());

        DisenchantEvent.onEvent(event);
        DisenchantEvent.onEvent(event);
        DisenchantEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
        assertNotNull(player.nextMessage(), "first attempt is explained");
        assertNull(player.nextMessage(), "keystroke spam is debounced");
    }

    @Test
    void givenHintDisabled_whenDisenchantPrepareAtRefusedAnvil_thenSilent() {
        setConfig("special-anvils.mode", "MARKED");
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());

        DisenchantEvent.onEvent(event);

        assertNull(player.nextMessage());
    }

    @Test
    void givenHintEnabled_whenShatterPrepareAtRefusedAnvil_thenHintSent() {
        setConfig("special-anvils.mode", "MARKED");
        setConfig("special-anvils.hint-message", true);
        PrepareAnvilEvent event = prepare(enchantedBook("sharpness", "mending"), new ItemStack(Material.BOOK), anvilLocation());

        ShatterEvent.onEvent(event);

        assertNotNull(player.nextMessage());
    }

    @Test
    void givenHintEnabled_whenAnvilAllowed_thenNoHint() {
        setConfig("special-anvils.hint-message", true);
        PrepareAnvilEvent event = prepare(sword(), new ItemStack(Material.BOOK), anvilLocation());

        DisenchantEvent.onEvent(event);

        assertNotNull(event.getResult());
        assertNull(player.nextMessage());
    }
}
