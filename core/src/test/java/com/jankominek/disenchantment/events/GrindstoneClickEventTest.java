package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.events.GrindstoneTestSupport.FakeGrindstone;
import com.jankominek.disenchantment.events.api.PostGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PostGrindstoneTransferEvent;
import com.jankominek.disenchantment.events.api.PreGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PreGrindstoneTransferEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the grindstone click handler: plain-book placement (the vanilla grindstone refuses a BOOK)
 * and the manual-result pattern for extract and transfer.
 */
class GrindstoneClickEventTest extends DisenchantmentTestBase {
    private static final int SOURCE = 0;
    private static final int SECOND = 1;
    private static final int RESULT = 2;

    private ItemStack enchanted(Material material, Object... enchantAndLevel) {
        ItemStack item = new ItemStack(material);
        for (int i = 0; i < enchantAndLevel.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) enchantAndLevel[i]), (Integer) enchantAndLevel[i + 1]);
        }
        return item;
    }

    private ItemStack books(int amount) {
        return new ItemStack(Material.BOOK, amount);
    }

    private ItemStack someResult() {
        return new ItemStack(Material.ENCHANTED_BOOK);
    }

    private PlayerMock playerWithLevels(int levels) {
        PlayerMock player = server.addPlayer();
        player.setLevel(levels);
        return player;
    }

    private InventoryClickEvent clickResult(PlayerMock player, FakeGrindstone grindstone) {
        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, RESULT, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(event);
        return event;
    }

    private static void assertCancelled(InventoryClickEvent event) {
        Mockito.verify(event).setCancelled(true);
    }

    private static void assertNotCancelled(InventoryClickEvent event) {
        Mockito.verify(event, Mockito.never()).setCancelled(true);
    }

    // ====================================================================================================
    // Book placement

    @Test
    void givenExtractEnabledAndCursorBook_whenClickSlotOne_thenExactlyOneBookPlaced() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.setItemOnCursor(books(5));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(event);

        assertCancelled(event);
        assertEquals(1, grindstone.slot(SECOND).getAmount(), "A left click must not dump the whole stack");
        assertEquals(Material.BOOK, grindstone.slot(SECOND).getType());
        assertEquals(4, player.getItemOnCursor().getAmount());
    }

    @Test
    void givenSingleBookOnCursor_whenRightClickSlotOne_thenCursorEmpty() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.setItemOnCursor(books(1));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        GrindstoneClickEvent.onEvent(GrindstoneTestSupport.click(player, grindstone, SECOND, ClickType.RIGHT));

        assertEquals(Material.BOOK, grindstone.slot(SECOND).getType());
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    @Test
    void givenFeatureDisabledOrNothingToExtract_whenClickSlotOneWithBook_thenVanillaHandlesIt() {
        PlayerMock player = playerWithLevels(0);
        player.setItemOnCursor(books(1));

        // Disabled by default
        FakeGrindstone disabled = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);
        InventoryClickEvent event = GrindstoneTestSupport.click(player, disabled, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(event);
        assertNotCancelled(event);
        assertNull(disabled.slot(SECOND));

        setConfig("grindstone.enabled", true);

        // Slot 0 empty, only a curse, a stack, or a book in slot 0 -> nothing to extract
        for (ItemStack source : new ItemStack[]{
                null,
                enchanted(Material.DIAMOND_SWORD, "binding_curse", 1),
                new ItemStack(Material.DIAMOND_SWORD),
                new ItemStack(Material.ENCHANTED_BOOK)
        }) {
            FakeGrindstone grindstone = new FakeGrindstone(player, source, null, null);
            InventoryClickEvent click = GrindstoneTestSupport.click(player, grindstone, SECOND, ClickType.LEFT);
            GrindstoneClickEvent.onEvent(click);
            assertNotCancelled(click);
            assertNull(grindstone.slot(SECOND));
        }

        ItemStack stack = enchanted(Material.DIAMOND_SWORD, "sharpness", 5);
        stack.setAmount(2);
        FakeGrindstone stacked = new FakeGrindstone(player, stack, null, null);
        InventoryClickEvent click = GrindstoneTestSupport.click(player, stacked, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(click);
        assertNotCancelled(click);
        assertEquals(1, player.getItemOnCursor().getAmount(), "The cursor is untouched");
    }

    @Test
    void givenOccupiedSlotOrTaggedBook_whenClickSlotOne_thenNoInterception() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        ItemStack source = enchanted(Material.DIAMOND_SWORD, "sharpness", 5);

        player.setItemOnCursor(books(1));
        FakeGrindstone occupied = new FakeGrindstone(player, source, books(1), null);
        InventoryClickEvent event = GrindstoneTestSupport.click(player, occupied, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(event);
        assertNotCancelled(event);

        ItemStack tagged = books(1);
        ItemMeta meta = tagged.getItemMeta();
        meta.setDisplayName("Quest book");
        tagged.setItemMeta(meta);
        player.setItemOnCursor(tagged);
        FakeGrindstone free = new FakeGrindstone(player, source, null, null);
        InventoryClickEvent taggedClick = GrindstoneTestSupport.click(player, free, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(taggedClick);
        assertNotCancelled(taggedClick);
        assertNull(free.slot(SECOND));
    }

    @Test
    void givenNoPermission_whenClickSlotOneWithBook_thenNoInterception() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.addAttachment(plugin, "disenchantment.grindstone.extract", false);
        player.addAttachment(plugin, "disenchantment.grindstone.all", false);
        player.setItemOnCursor(books(1));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, SECOND, ClickType.LEFT);
        GrindstoneClickEvent.onEvent(event);

        assertNotCancelled(event);
        assertNull(grindstone.slot(SECOND));
    }

    @Test
    void givenShiftClickOnBookStack_whenSourcePresent_thenOneBookMovesToSlotOne() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.getInventory().setItem(5, books(3));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        InventoryClickEvent event = GrindstoneTestSupport.clickPlayerInventory(player, grindstone, 5, ClickType.SHIFT_LEFT);
        GrindstoneClickEvent.onEvent(event);

        assertCancelled(event);
        assertEquals(1, grindstone.slot(SECOND).getAmount());
        assertEquals(2, player.getInventory().getItem(5).getAmount());
    }

    @Test
    void givenShiftClickOnLastBook_whenSourcePresent_thenInventorySlotCleared() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.getInventory().setItem(5, books(1));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        GrindstoneClickEvent.onEvent(GrindstoneTestSupport.clickPlayerInventory(player, grindstone, 5, ClickType.SHIFT_LEFT));

        assertEquals(Material.BOOK, grindstone.slot(SECOND).getType());
        assertTrue(player.getInventory().getItem(5) == null || player.getInventory().getItem(5).getType().isAir());
    }

    @Test
    void givenShiftClickOnOtherItemOrWithoutSource_whenClick_thenVanillaHandlesIt() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.getInventory().setItem(5, new ItemStack(Material.DIAMOND_SWORD));
        player.getInventory().setItem(6, books(2));

        FakeGrindstone withSource = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);
        InventoryClickEvent sword = GrindstoneTestSupport.clickPlayerInventory(player, withSource, 5, ClickType.SHIFT_LEFT);
        GrindstoneClickEvent.onEvent(sword);
        assertNotCancelled(sword);

        FakeGrindstone noSource = new FakeGrindstone(player, null, null, null);
        InventoryClickEvent book = GrindstoneTestSupport.clickPlayerInventory(player, noSource, 6, ClickType.SHIFT_LEFT);
        GrindstoneClickEvent.onEvent(book);
        assertNotCancelled(book);
        assertEquals(2, player.getInventory().getItem(6).getAmount());
        assertNull(noSource.slot(SECOND));
    }

    @Test
    void givenNonPlainClickTypesOnSlotOne_whenCursorBook_thenIgnored() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.setItemOnCursor(books(1));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        for (ClickType type : new ClickType[]{ClickType.SHIFT_LEFT, ClickType.DOUBLE_CLICK, ClickType.NUMBER_KEY, ClickType.DROP}) {
            InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, SECOND, type);
            GrindstoneClickEvent.onEvent(event);
            assertNotCancelled(event);
        }
        assertNull(grindstone.slot(SECOND));
    }

    // ====================================================================================================
    // Extract: result click

    @Test
    void givenValidExtract_whenClickResult_thenMovedStrippedChargedAndBookConsumed() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "unbreaking", 3), books(1), someResult());

        AtomicReference<PostGrindstoneExtractEvent> post = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostGrindstoneExtractEvent e) {
                post.set(e);
            }
        }, plugin);

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        ItemStack source = grindstone.slot(SOURCE);
        assertNotNull(source, "The source comes back into slot 0");
        assertEquals(Material.DIAMOND_SWORD, source.getType());
        assertTrue(source.getEnchantments().isEmpty(), "Moved, not copied");
        assertNull(grindstone.slot(SECOND), "Exactly the one book is consumed");
        assertNull(grindstone.slot(RESULT));

        ItemStack delivered = player.getItemOnCursor();
        assertEquals(Material.ENCHANTED_BOOK, delivered.getType());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) delivered.getItemMeta();
        assertEquals(5, meta.getStoredEnchantLevel(enchantment("sharpness")));
        assertEquals(3, meta.getStoredEnchantLevel(enchantment("unbreaking")));

        // default: base 10 + (5 + 3 * 2) * 0.25 -> 10 + 1.25 + 1.5 = 12.75 -> 13 levels
        assertEquals(7, player.getLevel());
        assertNotNull(post.get());
        assertEquals(13, post.get().getXpCost());
        assertEquals(0.0, post.get().getEconomyCost());
        assertEquals(Material.ENCHANTED_BOOK, post.get().getResultBook().getType());
    }

    @Test
    void givenBookStack_whenClickResult_thenOnlyOneBookConsumed() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(3), someResult());

        clickResult(player, grindstone);

        assertEquals(2, grindstone.slot(SECOND).getAmount());
        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType());
    }

    @Test
    void givenNotEnoughLevels_whenClickResult_thenNothingChangesAndPlayerIsTold() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(2);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size(), "Source untouched");
        assertEquals(1, grindstone.slot(SECOND).getAmount(), "Book untouched");
        assertNotNull(grindstone.slot(RESULT));
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(2, player.getLevel());
        assertNotNull(player.nextMessage(), "The player is told they need levels");
    }

    @Test
    void givenUnsafeClickTypes_whenResultClick_thenCancelledWithoutAnyChange() {
        setConfig("grindstone.enabled", true);

        for (ClickType type : new ClickType[]{ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT, ClickType.NUMBER_KEY, ClickType.DOUBLE_CLICK, ClickType.DROP, ClickType.CONTROL_DROP, ClickType.SWAP_OFFHAND}) {
            PlayerMock player = playerWithLevels(20);
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

            InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, RESULT, type);
            GrindstoneClickEvent.onEvent(event);

            assertCancelled(event);
            assertEquals(20, player.getLevel(), type + ": no charge");
            assertTrue(player.getItemOnCursor().getType().isAir(), type + ": no result delivered (no dupe)");
            assertEquals(1, grindstone.slot(SECOND).getAmount(), type + ": book kept");
            assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size(), type + ": source kept");
        }
    }

    @Test
    void givenCursorHoldsItem_whenResultClick_thenCancelledAndNothingChanges() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        player.setItemOnCursor(new ItemStack(Material.STICK));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(Material.STICK, player.getItemOnCursor().getType());
        assertEquals(20, player.getLevel());
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
    }

    @Test
    void givenCooldown_whenResultClick_thenCancelledAndUnchanged() {
        setConfig("grindstone.enabled", true);
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = playerWithLevels(20);
        CooldownManager.recordOperation(player);
        try {
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

            InventoryClickEvent event = clickResult(player, grindstone);

            assertCancelled(event);
            assertEquals(20, player.getLevel());
            assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        } finally {
            CooldownManager.reset();
        }
    }

    @Test
    void givenSuccessfulOperation_whenSecondClickRightAfter_thenCooldownStopsIt() {
        setConfig("grindstone.enabled", true);
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = playerWithLevels(40);
        try {
            FakeGrindstone first = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());
            clickResult(player, first);
            player.setItemOnCursor(new ItemStack(Material.AIR));

            FakeGrindstone second = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "mending", 1), books(1), someResult());
            clickResult(player, second);

            assertTrue(player.getItemOnCursor().getType().isAir(), "The cooldown recorded by the first operation applies");
            assertEquals(1, second.slot(SOURCE).getEnchantments().size());
        } finally {
            CooldownManager.reset();
        }
    }

    @Test
    void givenNoPermission_whenResultClick_thenCancelledAndUnchanged() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        player.addAttachment(plugin, "disenchantment.grindstone.extract", false);
        player.addAttachment(plugin, "disenchantment.grindstone.all", false);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(20, player.getLevel());
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    @Test
    void givenListenerCancelsPreEvent_whenResultClick_thenNothingChanges() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreGrindstoneExtractEvent e) {
                e.setCancelled(true);
            }
        }, plugin);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(20, player.getLevel(), "No charge when a Pre event vetoes");
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertEquals(1, grindstone.slot(SECOND).getAmount());
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    @Test
    void givenListenerVetoesOneEnchantment_whenResultClick_thenItStaysOnTheSource() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreGrindstoneExtractEvent e) {
                e.getEnchantments().removeIf(en -> en.getKey().equals("mending"));
            }
        }, plugin);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "mending", 1), books(1), someResult());

        clickResult(player, grindstone);

        assertTrue(grindstone.slot(SOURCE).containsEnchantment(enchantment("mending")), "Vetoed enchantment stays");
        assertFalse(grindstone.slot(SOURCE).containsEnchantment(enchantment("sharpness")));
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) player.getItemOnCursor().getItemMeta();
        assertFalse(meta.hasStoredEnchant(enchantment("mending")));
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
    }

    @Test
    void givenCurseAndKeepState_whenResultClick_thenTheyStayOnTheSource() {
        setConfig("grindstone.enabled", true);
        setDisenchantEnchantmentStates(List.of("mending:KEEP"));
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "mending", 1, "binding_curse", 1), books(1), someResult());

        clickResult(player, grindstone);

        ItemStack source = grindstone.slot(SOURCE);
        assertTrue(source.containsEnchantment(enchantment("mending")));
        assertTrue(source.containsEnchantment(enchantment("binding_curse")));
        assertFalse(source.containsEnchantment(enchantment("sharpness")));
    }

    @Test
    void givenDeleteState_whenResultClick_thenDestroyedOnSourceAndNotOnBook() {
        setConfig("grindstone.enabled", true);
        setDisenchantEnchantmentStates(List.of("mending:DELETE"));
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "mending", 1), books(1), someResult());

        clickResult(player, grindstone);

        assertTrue(grindstone.slot(SOURCE).getEnchantments().isEmpty());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) player.getItemOnCursor().getItemMeta();
        assertFalse(meta.hasStoredEnchant(enchantment("mending")));
        assertTrue(meta.hasStoredEnchant(enchantment("sharpness")));
    }

    @Test
    void givenChanceZero_whenResultClick_thenPlainBookDeliveredAndSourceStillStripped() {
        setConfig("grindstone.enabled", true);
        setConfig("disenchantment.enchantment-chances.sharpness", 0.0);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        clickResult(player, grindstone);

        assertEquals(Material.BOOK, player.getItemOnCursor().getType());
        assertTrue(grindstone.slot(SOURCE).getEnchantments().isEmpty());
        assertNull(grindstone.slot(SECOND));
    }

    @Test
    void givenCreative_whenResultClick_thenNoChargeAndPostReportsZero() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.setGameMode(GameMode.CREATIVE);
        AtomicReference<PostGrindstoneExtractEvent> post = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostGrindstoneExtractEvent e) {
                post.set(e);
            }
        }, plugin);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        clickResult(player, grindstone);

        assertEquals(0, player.getLevel());
        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType());
        assertEquals(0, post.get().getXpCost());
    }

    @Test
    void givenEmptyOrForeignResultSlot_whenResultClick_thenNotHandled() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);

        FakeGrindstone empty = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), null);
        InventoryClickEvent event = clickResult(player, empty);
        assertNotCancelled(event);

        FakeGrindstone foreign = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), new ItemStack(Material.DIAMOND_SWORD));
        InventoryClickEvent foreignEvent = clickResult(player, foreign);
        assertNotCancelled(foreignEvent);
        assertEquals(20, player.getLevel());
    }

    @Test
    void givenExtractDisabled_whenResultClick_thenVanillaUntouched() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertNotCancelled(event);
        assertEquals(20, player.getLevel());
    }

    @Test
    void givenVanillaGrindstoneUse_whenResultClick_thenNotCancelled() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(20);
        // Single enchanted sword: the vanilla "disenchant" grind
        FakeGrindstone single = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, new ItemStack(Material.DIAMOND_SWORD));
        assertNotCancelled(clickResult(player, single));

        // Two swords: the vanilla repair
        FakeGrindstone repair = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND_SWORD));
        assertNotCancelled(clickResult(player, repair));
    }

    @Test
    void givenEconomyEnabled_whenResultClick_thenChargedOnceAndReported() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.economy.enabled", true);
        setConfig("grindstone.economy.cost", 25.0);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = playerWithLevels(20);
            AtomicReference<PostGrindstoneExtractEvent> post = new AtomicReference<>();
            server.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void on(PostGrindstoneExtractEvent e) {
                    post.set(e);
                }
            }, plugin);
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

            clickResult(player, grindstone);

            Mockito.verify(economy, Mockito.times(1)).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.eq(25.0));
            assertEquals(25.0, post.get().getEconomyCost());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenFailedWithdrawal_whenResultClick_thenNothingChanges() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble()))
                .thenReturn(new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "bank limit"));
        try {
            PlayerMock player = playerWithLevels(20);
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

            InventoryClickEvent event = clickResult(player, grindstone);

            assertCancelled(event);
            assertEquals(20, player.getLevel());
            assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
            assertEquals(1, grindstone.slot(SECOND).getAmount());
            assertTrue(player.getItemOnCursor().getType().isAir());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenMaintenanceMode_whenResultClick_thenCancelledAndPlayerTold() {
        setConfig("grindstone.enabled", true);
        com.jankominek.disenchantment.Disenchantment.maintenanceEnabled = true;
        try {
            PlayerMock player = playerWithLevels(20);
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), null);

            InventoryClickEvent event = clickResult(player, grindstone);

            assertCancelled(event);
            assertNotNull(player.nextMessage());
            assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        } finally {
            com.jankominek.disenchantment.Disenchantment.maintenanceEnabled = false;
        }
    }

    // ====================================================================================================
    // Transfer: result click

    private void flatTransferCost(int levels) {
        setConfig("grindstone.transfer.repair.base", (double) levels);
        setConfig("grindstone.transfer.repair.multiply", 0.0);
    }

    @Test
    void givenValidTransfer_whenClickResult_thenMovedNotCopiedAndTargetConsumed() {
        setConfig("grindstone.transfer.enabled", true);
        flatTransferCost(4);
        PlayerMock player = playerWithLevels(10);
        ItemStack source = enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5, "unbreaking", 3);
        FakeGrindstone grindstone = new FakeGrindstone(player, source, new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

        AtomicReference<PostGrindstoneTransferEvent> post = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PostGrindstoneTransferEvent e) {
                post.set(e);
            }
        }, plugin);

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        ItemStack delivered = player.getItemOnCursor();
        assertEquals(Material.IRON_SHOVEL, delivered.getType());
        assertEquals(5, delivered.getEnchantmentLevel(enchantment("efficiency")));
        assertEquals(3, delivered.getEnchantmentLevel(enchantment("unbreaking")));

        assertEquals(Material.DIAMOND_PICKAXE, grindstone.slot(SOURCE).getType());
        assertTrue(grindstone.slot(SOURCE).getEnchantments().isEmpty(), "The source lost exactly what the target gained");
        assertNull(grindstone.slot(SECOND), "The target is consumed - it is the item on the cursor now");
        assertNull(grindstone.slot(RESULT));
        assertEquals(6, player.getLevel());
        assertEquals(4, post.get().getXpCost());
        assertEquals(2, post.get().getEnchantments().size());
    }

    @Test
    void givenPartlyCompatibleTransfer_whenClickResult_thenIncompatibleStayOnSource() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(30);
        // sharpness cannot go on a pickaxe
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_SWORD, "sharpness", 5, "unbreaking", 3), new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.IRON_PICKAXE));

        clickResult(player, grindstone);

        assertEquals(5, grindstone.slot(SOURCE).getEnchantmentLevel(enchantment("sharpness")));
        assertFalse(grindstone.slot(SOURCE).containsEnchantment(enchantment("unbreaking")));
        assertEquals(3, player.getItemOnCursor().getEnchantmentLevel(enchantment("unbreaking")));
        assertFalse(player.getItemOnCursor().containsEnchantment(enchantment("sharpness")));
    }

    @Test
    void givenNotEnoughLevels_whenTransferClick_thenNothingChanges() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(1);
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertNotNull(grindstone.slot(SECOND));
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertEquals(1, player.getLevel());
    }

    @Test
    void givenUnsafeClick_whenTransferResultClick_thenNoDupe() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(30);
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, RESULT, ClickType.SHIFT_LEFT);
        GrindstoneClickEvent.onEvent(event);

        assertCancelled(event);
        assertEquals(30, player.getLevel());
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertNotNull(grindstone.slot(SECOND));
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    @Test
    void givenListenerCancelsPreTransfer_whenClickResult_thenNothingChanges() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(30);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(PreGrindstoneTransferEvent e) {
                e.setCancelled(true);
            }
        }, plugin);
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(30, player.getLevel());
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertNotNull(grindstone.slot(SECOND));
        assertTrue(player.getItemOnCursor().getType().isAir());
    }

    @Test
    void givenTransferEconomy_whenClickResult_thenChargedFromTransferBlock() {
        setConfig("grindstone.transfer.enabled", true);
        setConfig("grindstone.transfer.economy.enabled", true);
        setConfig("grindstone.transfer.economy.cost", 77.0);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = playerWithLevels(30);
            FakeGrindstone grindstone = new FakeGrindstone(player,
                    enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

            clickResult(player, grindstone);

            Mockito.verify(economy, Mockito.times(1)).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.eq(77.0));
            assertEquals(Material.IRON_SHOVEL, player.getItemOnCursor().getType());
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenNoTransferPermission_whenClickResult_thenNothingChanges() {
        setConfig("grindstone.transfer.enabled", true);
        PlayerMock player = playerWithLevels(30);
        player.addAttachment(plugin, "disenchantment.grindstone.transfer", false);
        player.addAttachment(plugin, "disenchantment.grindstone.all", false);
        FakeGrindstone grindstone = new FakeGrindstone(player,
                enchanted(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.IRON_SHOVEL), new ItemStack(Material.IRON_SHOVEL));

        InventoryClickEvent event = clickResult(player, grindstone);

        assertCancelled(event);
        assertEquals(1, grindstone.slot(SOURCE).getEnchantments().size());
        assertEquals(30, player.getLevel());
    }

    // ====================================================================================================
    // Guards

    @Test
    void givenNonGrindstoneInventory_whenClick_thenIgnored() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());
        Mockito.when(grindstone.inventory.getType()).thenReturn(org.bukkit.event.inventory.InventoryType.ANVIL);

        InventoryClickEvent event = clickResult(player, grindstone);

        assertNotCancelled(event);
        assertEquals(20, player.getLevel());
    }

    @Test
    void givenPluginDisabled_whenClick_thenIgnored() {
        setConfig("grindstone.enabled", true);
        setConfig("enabled", false);
        PlayerMock player = playerWithLevels(20);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        InventoryClickEvent event = clickResult(player, grindstone);

        assertNotCancelled(event);
        assertEquals(20, player.getLevel());
    }

    // ====================================================================================================
    // An earlier veto (protection or shop plugin) wins

    @Test
    void givenResultClickAlreadyCancelled_whenClick_thenNothingChargedOrMoved() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(20);
        ItemStack sword = enchanted(Material.DIAMOND_SWORD, "sharpness", 5);
        FakeGrindstone grindstone = new FakeGrindstone(player, sword, books(1), someResult());

        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, RESULT, ClickType.LEFT);
        Mockito.when(event.isCancelled()).thenReturn(true);
        GrindstoneClickEvent.onEvent(event);

        assertEquals(20, player.getLevel());
        assertTrue(player.getItemOnCursor().getType().isAir());
        assertSame(sword, grindstone.slot(SOURCE));
        assertNotNull(grindstone.slot(SECOND));
        assertNotNull(grindstone.slot(RESULT));
    }

    @Test
    void givenBookPlacementAlreadyCancelled_whenClickSlotOne_thenNoBookMoved() {
        setConfig("grindstone.enabled", true);
        PlayerMock player = playerWithLevels(0);
        player.setItemOnCursor(books(5));
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), null, null);

        InventoryClickEvent event = GrindstoneTestSupport.click(player, grindstone, SECOND, ClickType.LEFT);
        Mockito.when(event.isCancelled()).thenReturn(true);
        GrindstoneClickEvent.onEvent(event);

        assertNull(grindstone.slot(SECOND));
        assertEquals(5, player.getItemOnCursor().getAmount());
    }

    // The grindstone has no "Too Expensive!" screen, so the 40 cap never applies: only the player's levels limit it.
    @Test
    void givenExtractCostOver40_whenClickResult_thenStillDeliveredWhenAffordable() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.repair.base", 45);
        PlayerMock player = playerWithLevels(60);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        clickResult(player, grindstone);

        assertEquals(Material.ENCHANTED_BOOK, player.getItemOnCursor().getType());
        assertTrue(player.getLevel() < 60);
    }

    @Test
    void givenMaxCostSet_whenExtract_thenChargeIsCapped() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.repair.base", 45);
        setConfig("grindstone.repair.max-cost", 7);
        PlayerMock player = playerWithLevels(60);
        FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());

        clickResult(player, grindstone);

        assertEquals(53, player.getLevel());
    }

    // ====================================================================================================
    // Charge ordering

    @Test
    void givenAdapterThrowsWhileBuildingExtract_whenClickResult_thenNothingCharged() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.economy.enabled", true);
        activateMockPlugin(new com.jankominek.disenchantment.plugins.MockPluginAdapter("Test", new SettleOrderingTest.BoomEnchantment()));
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = playerWithLevels(20);
            ItemStack sword = enchanted(Material.DIAMOND_SWORD, "sharpness", 5);
            FakeGrindstone grindstone = new FakeGrindstone(player, sword, books(1), someResult());

            clickResult(player, grindstone);

            assertEquals(20, player.getLevel(), "XP must not be taken when the result could not be built");
            Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
            assertTrue(player.getItemOnCursor().getType().isAir());
            assertSame(sword, grindstone.slot(SOURCE), "The source is untouched");
        } finally {
            EconomyTestSupport.uninstall();
        }
    }

    @Test
    void givenSlotWriteFailsAfterPayment_whenClickResult_thenXpAndMoneyRefunded() {
        setConfig("grindstone.enabled", true);
        setConfig("grindstone.economy.enabled", true);
        Economy economy = EconomyTestSupport.install();
        try {
            PlayerMock player = playerWithLevels(20);
            FakeGrindstone grindstone = new FakeGrindstone(player, enchanted(Material.DIAMOND_SWORD, "sharpness", 5), books(1), someResult());
            Mockito.doThrow(new IllegalStateException("slot write failed")).when(grindstone.inventory).setItem(Mockito.eq(SOURCE), Mockito.any());

            clickResult(player, grindstone);

            assertEquals(20, player.getLevel(), "XP is restored");
            Mockito.verify(economy).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.eq(100.0));
            Mockito.verify(economy).depositPlayer(Mockito.any(OfflinePlayer.class), Mockito.eq(100.0));
        } finally {
            EconomyTestSupport.uninstall();
        }
    }
}
