package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.GrindstoneTestSupport.FakeGrindstone;
import com.jankominek.disenchantment.utils.CooldownManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the grindstone prepare logic: which result is shown for which slot contents.
 */
class GrindstoneEventTest extends DisenchantmentTestBase {

    private static final class Capture {
        final AtomicInteger calls = new AtomicInteger();
        final AtomicReference<ItemStack> result = new AtomicReference<>();

        void accept(ItemStack item) {
            calls.incrementAndGet();
            result.set(item);
        }
    }

    private ItemStack enchanted(Material material, Object... enchantAndLevel) {
        ItemStack item = new ItemStack(material);
        for (int i = 0; i < enchantAndLevel.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) enchantAndLevel[i]), (Integer) enchantAndLevel[i + 1]);
        }
        return item;
    }

    private Capture prepare(PlayerMock player, ItemStack slot0, ItemStack slot1) {
        FakeGrindstone grindstone = new FakeGrindstone(player, slot0, slot1, null);
        Capture capture = new Capture();
        GrindstoneEvent.prepare(player, grindstone.inventory, capture::accept);
        return capture;
    }

    // -> master switches

    @Test
    void givenDefaultConfig_whenPrepare_thenVanillaUntouched() {
        PlayerMock player = server.addPlayer();

        Capture capture = prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK));

        assertEquals(0, capture.calls.get(), "Both features are opt-in");
    }

    // -> extract

    @Test
    void givenExtractEnabled_whenSwordAndBlankBook_thenEnchantedBookResult() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();

        Capture capture = prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "unbreaking", 3), new ItemStack(Material.BOOK));

        ItemStack result = capture.result.get();
        assertNotNull(result);
        assertEquals(Material.ENCHANTED_BOOK, result.getType());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
        assertEquals(5, meta.getStoredEnchantLevel(enchantment("sharpness")));
        assertEquals(3, meta.getStoredEnchantLevel(enchantment("unbreaking")));
    }

    @Test
    void givenExtractEnabled_whenNothingExtractable_thenNoResultSet() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();

        assertEquals(0, prepare(player, new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK)).calls.get());
        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_SWORD, "binding_curse", 1), new ItemStack(Material.BOOK)).calls.get(),
                "Curses stay on the item");

        ItemStack named = new ItemStack(Material.BOOK);
        ItemMeta meta = named.getItemMeta();
        meta.setDisplayName("Voucher");
        named.setItemMeta(meta);
        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), named).calls.get(),
                "A tagged book is not a blank book");
    }

    @Test
    void givenExtractEnabled_whenWorldDisabled_thenNoResult() {
        setConfig("grindstone.enabled", true);
        setConfig("disenchantment.worlds.list", List.of(world().getName()));
        PlayerMock player = server.addPlayer();

        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK)).calls.get());
    }

    @Test
    void givenExtractEnabled_whenPlayerLacksPermission_thenNoResult() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.grindstone.extract", false);
        player.addAttachment(plugin, "disenchantment.grindstone.all", false);

        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK)).calls.get());
    }

    @Test
    void givenExtractEnabled_whenOnCooldown_thenResultCleared() {
        setConfig("grindstone.enabled", true);
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer();
        CooldownManager.recordOperation(player);
        try {
            Capture capture = prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK));

            assertEquals(1, capture.calls.get());
            assertNull(capture.result.get(), "The result is cleared while on cooldown");
        } finally {
            CooldownManager.reset();
        }
    }

    // -> transfer

    @Test
    void givenTransferEnabled_whenDifferentItems_thenTargetWithEnchantmentsResult() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = server.addPlayer();

        Capture capture = prepare(player, enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5, "fortune", 3), new ItemStack(Material.IRON_SHOVEL));

        ItemStack result = capture.result.get();
        assertNotNull(result);
        assertEquals(Material.IRON_SHOVEL, result.getType());
        assertEquals(5, result.getEnchantmentLevel(enchantment("efficiency")));
        assertEquals(3, result.getEnchantmentLevel(enchantment("fortune")));
    }

    @Test
    void givenTransferEnabled_whenSameMaterialOrBook_thenVanillaUntouched() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = server.addPlayer();
        ItemStack source = enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5);

        assertEquals(0, prepare(player, source, new ItemStack(Material.DIAMOND_PICKAXE)).calls.get(), "Vanilla repair");
        assertEquals(0, prepare(player, source, new ItemStack(Material.BOOK)).calls.get(), "Books are the extract mode");
        assertEquals(0, prepare(player, source, null).calls.get());
    }

    @Test
    void givenOnlyTransferEnabled_whenBlankBook_thenExtractNotOffered() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = server.addPlayer();

        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK)).calls.get());
    }

    @Test
    void givenOnlyExtractEnabled_whenDifferentItems_thenTransferNotOffered() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();

        assertEquals(0, prepare(player, enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL)).calls.get());
    }

    // -> the event adapter: PrepareGrindstoneEvent is only known by shape

    @Test
    void givenEventWithoutCompileTimeClass_whenOnEvent_thenResultSetThroughReflection() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK), null);

        FakePrepareEvent event = Mockito.mock(FakePrepareEvent.class);
        Mockito.when(event.getInventory()).thenReturn((Inventory) grindstone.inventory);

        GrindstoneEvent.onEvent(event);

        org.mockito.ArgumentCaptor<ItemStack> captor = org.mockito.ArgumentCaptor.forClass(ItemStack.class);
        Mockito.verify(event).setResult(captor.capture());
        assertEquals(Material.ENCHANTED_BOOK, captor.getValue().getType());
    }

    @Test
    void givenNonGrindstoneInventory_whenOnEvent_thenIgnored() {
        setConfig("grindstone.enabled", true);

        FakePrepareEvent event = Mockito.mock(FakePrepareEvent.class);
        Mockito.when(event.getInventory()).thenReturn(Mockito.mock(Inventory.class));

        GrindstoneEvent.onEvent(event);

        Mockito.verify(event, Mockito.never()).setResult(Mockito.any());
    }

    // -> tick-based fallback

    @Test
    void givenNoPrepareEvent_whenRefresh_thenResultWrittenIntoSlotTwo() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.BOOK), null);

        GrindstoneEvent.scheduleRefresh(player, grindstone.inventory);
        assertNull(grindstone.slot(2), "Nothing happens before the next tick");
        server.getScheduler().performOneTick();

        assertNotNull(grindstone.slot(2));
        assertEquals(Material.ENCHANTED_BOOK, grindstone.slot(2).getType());
    }

    @Test
    void givenNothingToDo_whenRefresh_thenResultSlotLeftAlone() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = server.addPlayer();
        ItemStack vanillaResult = new ItemStack(Material.DIAMOND_SWORD);
        FakeGrindstone grindstone = new FakeGrindstone(player, new ItemStack(Material.DIAMOND_SWORD), null, vanillaResult);

        GrindstoneEvent.refresh(player, grindstone.inventory);

        assertSame(vanillaResult, grindstone.slot(2));
    }
}
