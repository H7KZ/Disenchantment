package com.jankominek.disenchantment.guis;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PostEraseEvent;
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI;
import com.jankominek.disenchantment.utils.CooldownManager;
import com.jankominek.disenchantment.utils.EconomyTestSupport;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.PreviewLore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Behaviour of the Bedrock chest menu: explicit item movement (the click router cancels everything), the
 * guarantee that no item is ever lost or duplicated, and that confirming runs the real operation once.
 */
class BedrockAnvilGUITest extends DisenchantmentTestBase {

    private PlayerMock player;
    private AtomicInteger clock;
    private final AtomicInteger disenchanted = new AtomicInteger();
    private final AtomicInteger shattered = new AtomicInteger();
    private final AtomicInteger erased = new AtomicInteger();

    @BeforeEach
    void setUp() {
        setConfig("bedrock.fallback-enabled", true);
        player = server.addPlayer(".Steve");
        player.setLevel(30);
        // Teleport before any block is marked: MockBukkit forgets chunk data when the player's chunk is (re)loaded.
        player.teleport(new Location(world(), 0.5, 64, 0.5));

        // A controllable clock for the confirm debounce; starts far from "zero" so the first confirm passes.
        clock = new AtomicInteger(1_000_000);
        BedrockAnvilGUI.setClock(clock::get);

        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDisenchant(PostDisenchantEvent e) {
                disenchanted.incrementAndGet();
            }

            @EventHandler
            public void onShatter(PostShatterEvent e) {
                shattered.incrementAndGet();
            }

            @EventHandler
            public void onErase(PostEraseEvent e) {
                erased.incrementAndGet();
            }
        }, plugin);
    }

    @AfterEach
    void tearDown() {
        BedrockAnvilGUI.setClock(null);
        BedrockAnvilGUI.closeAll();
        CooldownManager.reset();
        Disenchantment.maintenanceEnabled = false;
        EconomyTestSupport.uninstall();
    }

    // -> helpers

    private BedrockAnvilGUI open() {
        assertTrue(BedrockAnvilGUI.tryOpen(player, null), "the menu should open");
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);
        assertNotNull(gui);
        return gui;
    }

    private Inventory menu() {
        return BedrockAnvilGUI.of(player).getInventory();
    }

    private InventoryClickEvent click(int rawSlot, ClickType type) {
        InventoryAction action = type.isShiftClick() ? InventoryAction.MOVE_TO_OTHER_INVENTORY : InventoryAction.PICKUP_ALL;
        InventoryClickEvent event = new InventoryClickEvent(player.getOpenInventory(),
                rawSlot < SIZE ? SlotType.CONTAINER : SlotType.CONTAINER, rawSlot, type, action);
        server.getPluginManager().callEvent(event);
        return event;
    }

    // Raw slot of a PlayerInventory index while the 27-slot menu is the top inventory.
    private static int raw(int playerSlot) {
        return playerSlot < 9 ? 54 + playerSlot : 27 + (playerSlot - 9);
    }

    private InventoryClickEvent tapInventory(int playerSlot) {
        return click(raw(playerSlot), ClickType.LEFT);
    }

    private InventoryClickEvent tapMenu(int slot) {
        return click(slot, ClickType.LEFT);
    }

    private void advanceClock() {
        clock.addAndGet(10_000);
    }

    private ItemStack sword(Object... keyLevelPairs) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        for (int i = 0; i < keyLevelPairs.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) keyLevelPairs[i]), (Integer) keyLevelPairs[i + 1]);
        }
        return item;
    }

    private ItemStack enchantedBook(String... keys) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        for (String key : keys) EnchantmentUtils.addStoredEnchantment(book, enchantment(key), 3);
        return book;
    }

    private void give(int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    private int count(Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) total += stack.getAmount();
        }
        for (int slot : new int[]{SLOT_ITEM, SLOT_SECOND}) {
            ItemStack stack = BedrockAnvilGUI.of(player) == null ? null : menu().getItem(slot);
            if (stack != null && stack.getType() == material) total += stack.getAmount();
        }
        for (Item dropped : player.getWorld().getEntitiesByClass(Item.class)) {
            if (dropped.getItemStack().getType() == material) total += dropped.getItemStack().getAmount();
        }
        return total;
    }

    // Fills every slot a stack could land in, armour and off-hand included: MockBukkit's addItem also uses those.
    private void fillInventoryWith(Material filler) {
        for (int i = 0; i < 36; i++) player.getInventory().setItem(i, new ItemStack(filler, 64));
        player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(filler, 64), new ItemStack(filler, 64), new ItemStack(filler, 64), new ItemStack(filler, 64)});
        player.getInventory().setItemInOffHand(new ItemStack(filler, 64));
    }

    private ItemStack firstOf(Material material) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) return stack;
        }
        return null;
    }

    // ----------------------------------------------------------------------------------------------------
    // Layout

    @Test
    void givenOpenedMenu_thenThreeRowsWithBordersLabelsAndButtons() {
        open();

        assertEquals(27, menu().getSize());
        assertEquals(Material.ANVIL, menu().getItem(SLOT_MODE).getType());
        assertEquals(Material.GRAY_DYE, menu().getItem(SLOT_CONFIRM).getType(), "nothing to confirm yet");
        assertEquals(Material.BARRIER, menu().getItem(SLOT_CANCEL).getType());
        assertNull(menu().getItem(SLOT_ITEM), "input slots start empty");
        assertNull(menu().getItem(SLOT_SECOND));
        assertEquals(Material.GRAY_STAINED_GLASS_PANE, menu().getItem(0).getType());
    }

    // ----------------------------------------------------------------------------------------------------
    // Item movement

    @Test
    void givenTapOnInventoryItem_thenExactlyOneMovesIntoTheItemSlot() {
        give(0, sword("sharpness", 5));
        open();

        InventoryClickEvent event = tapInventory(0);

        assertTrue(event.isCancelled());
        assertNull(player.getInventory().getItem(0), "the single sword left the inventory");
        assertEquals(Material.DIAMOND_SWORD, menu().getItem(SLOT_ITEM).getType());
        assertEquals(1, menu().getItem(SLOT_ITEM).getAmount());
    }

    @Test
    void givenTapOnMainInventoryItem_thenItIsMovedToo() {
        give(20, sword("sharpness", 5));
        open();

        tapInventory(20);

        assertNull(player.getInventory().getItem(20));
        assertEquals(Material.DIAMOND_SWORD, menu().getItem(SLOT_ITEM).getType());
    }

    @Test
    void givenStackOfBooks_whenTappedTwice_thenOneAtATimeAndTheyMerge() {
        give(0, new ItemStack(Material.BOOK, 3));
        open();

        tapInventory(0);
        tapInventory(0);

        assertEquals(Material.BOOK, menu().getItem(SLOT_SECOND).getType());
        assertEquals(2, menu().getItem(SLOT_SECOND).getAmount());
        assertEquals(1, player.getInventory().getItem(0).getAmount());
        assertNull(menu().getItem(SLOT_ITEM));
    }

    @Test
    void givenEnchantedBook_thenGoesToItemSlotAndPlainBookToSecondSlot() {
        give(0, enchantedBook("sharpness", "unbreaking"));
        give(1, new ItemStack(Material.BOOK, 2));
        open();

        tapInventory(0);
        tapInventory(1);

        assertEquals(Material.ENCHANTED_BOOK, menu().getItem(SLOT_ITEM).getType());
        assertEquals(Material.BOOK, menu().getItem(SLOT_SECOND).getType());
    }

    @Test
    void givenEraseEnabled_whenCatalystTapped_thenGoesToSecondSlot() {
        setConfig("erase.enabled", true);
        give(0, new ItemStack(Material.ECHO_SHARD, 4));
        open();

        tapInventory(0);

        assertEquals(Material.ECHO_SHARD, menu().getItem(SLOT_SECOND).getType());
        assertEquals(1, menu().getItem(SLOT_SECOND).getAmount());
        assertEquals(3, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void givenEraseDisabled_whenEchoShardTapped_thenItIsJustAnItem() {
        give(0, new ItemStack(Material.ECHO_SHARD));
        open();

        tapInventory(0);

        assertEquals(Material.ECHO_SHARD, menu().getItem(SLOT_ITEM).getType());
        assertNull(menu().getItem(SLOT_SECOND));
    }

    @Test
    void givenItemInSlot_whenSlotTapped_thenItGoesBackToTheInventory() {
        give(0, sword("sharpness", 5));
        open();
        tapInventory(0);

        InventoryClickEvent event = tapMenu(SLOT_ITEM);

        assertTrue(event.isCancelled());
        assertNull(menu().getItem(SLOT_ITEM));
        assertEquals(Material.DIAMOND_SWORD, firstOf(Material.DIAMOND_SWORD).getType());
        assertEquals(1, count(Material.DIAMOND_SWORD));
    }

    @Test
    void givenOccupiedItemSlot_whenAnotherItemTapped_thenTheOldOneIsReturnedAndNothingIsLost() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.DIAMOND_PICKAXE));
        open();
        tapInventory(0);

        tapInventory(1);

        assertEquals(Material.DIAMOND_PICKAXE, menu().getItem(SLOT_ITEM).getType());
        assertEquals(1, count(Material.DIAMOND_SWORD), "the swapped-out sword is back in the inventory");
        assertEquals(1, count(Material.DIAMOND_PICKAXE));
    }

    @Test
    void givenFullInventory_whenOccupiedSlotIsSwapped_thenNoItemIsLost() {
        open();
        player.getInventory().setItem(0, sword("sharpness", 5));
        tapInventory(0);
        // Fill every slot with a full stack, then offer a stack of two picks: tapping moves one, the swapped
        // sword has nowhere to go but the floor.
        fillInventoryWith(Material.DIRT);
        give(5, new ItemStack(Material.DIAMOND_PICKAXE, 2));

        tapInventory(5);

        assertEquals(1, count(Material.DIAMOND_SWORD), "the sword must still exist: inventory or floor");
        assertEquals(2, count(Material.DIAMOND_PICKAXE));
    }

    @Test
    void givenShiftClickOnInventoryItem_thenItIsHandledLikeATapAndCancelled() {
        give(0, sword("sharpness", 5));
        open();

        InventoryClickEvent event = click(raw(0), ClickType.SHIFT_LEFT);

        assertTrue(event.isCancelled());
        assertEquals(Material.DIAMOND_SWORD, menu().getItem(SLOT_ITEM).getType());
        assertNull(player.getInventory().getItem(0));
    }

    @Test
    void givenNumberKeyOrDoubleClickOrDrop_thenCancelledAndNothingMoves() {
        give(0, sword("sharpness", 5));
        open();

        for (ClickType type : List.of(ClickType.NUMBER_KEY, ClickType.DOUBLE_CLICK, ClickType.DROP, ClickType.CONTROL_DROP, ClickType.MIDDLE, ClickType.SWAP_OFFHAND)) {
            InventoryClickEvent event = click(raw(0), type);
            assertTrue(event.isCancelled(), type.name());
        }

        assertNull(menu().getItem(SLOT_ITEM));
        assertEquals(Material.DIAMOND_SWORD, player.getInventory().getItem(0).getType());
    }

    @Test
    void givenTapOnDisplayOnlySlots_thenNothingChanges() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        open();
        tapInventory(0);
        tapInventory(1);
        ItemStack before = menu().getItem(SLOT_RESULT).clone();

        for (int slot : new int[]{SLOT_RESULT, SLOT_MODE, 0, 4, 19, 21, 23}) {
            assertTrue(tapMenu(slot).isCancelled());
        }

        assertEquals(before, menu().getItem(SLOT_RESULT));
        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertEquals(1, count(Material.BOOK));
        assertEquals(0, disenchanted.get());
    }

    @Test
    void givenDrag_thenCancelled() {
        give(0, sword("sharpness", 5));
        open();
        ItemStack stack = new ItemStack(Material.DIAMOND, 8);
        org.bukkit.event.inventory.InventoryDragEvent drag = new org.bukkit.event.inventory.InventoryDragEvent(
                player.getOpenInventory(), stack, stack, false, java.util.Map.of(SLOT_ITEM, new ItemStack(Material.DIAMOND, 8)));

        server.getPluginManager().callEvent(drag);

        assertTrue(drag.isCancelled());
    }

    // ----------------------------------------------------------------------------------------------------
    // Close, quit, disable: inputs always come back

    @Test
    void givenItemsInSlots_whenMenuClosed_thenBothAreReturned() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK, 2));
        open();
        tapInventory(0);
        tapInventory(1);
        tapInventory(1);

        player.closeInventory();

        assertNull(BedrockAnvilGUI.of(player), "the session is gone");
        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertEquals(2, count(Material.BOOK));
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
        assertEquals(2, firstOf(Material.BOOK).getAmount());
    }

    @Test
    void givenFullInventory_whenMenuClosed_thenInputsAreDroppedAtTheFeet() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        open();
        tapInventory(0);
        tapInventory(1);
        fillInventoryWith(Material.DIRT);

        player.closeInventory();

        assertEquals(1, count(Material.DIAMOND_SWORD), "dropped, not destroyed");
        assertEquals(1, count(Material.BOOK));
        long dropped = player.getWorld().getEntitiesByClass(Item.class).stream()
                .filter(item -> item.getItemStack().getType() == Material.DIAMOND_SWORD).count();
        assertEquals(1, dropped);
    }

    @Test
    void givenCancelButton_thenInputsReturnedAndMenuClosed() {
        give(0, sword("sharpness", 5));
        open();
        tapInventory(0);

        tapMenu(SLOT_CANCEL);

        assertNull(BedrockAnvilGUI.of(player));
        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
    }

    @Test
    void givenReturnCalledTwiceOrCloseAfterCancel_thenNothingIsDuplicated() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK, 2));
        BedrockAnvilGUI gui = open();
        tapInventory(0);
        tapInventory(1);

        gui.returnInputs();
        gui.returnInputs();
        gui.finish();
        gui.onClose();
        player.closeInventory();

        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertEquals(2, count(Material.BOOK));
    }

    @Test
    void givenPlayerQuits_thenInputsAreReturnedFirst() {
        give(0, sword("sharpness", 5));
        open();
        tapInventory(0);

        BedrockAnvilGUI.onQuit(player);

        assertNull(BedrockAnvilGUI.of(player));
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
        assertEquals(1, count(Material.DIAMOND_SWORD));
    }

    @Test
    void givenPluginDisables_thenEveryOpenMenuGivesItemsBackAndCloses() {
        PlayerMock other = server.addPlayer(".Alex");
        give(0, sword("sharpness", 5));
        other.getInventory().setItem(0, new ItemStack(Material.DIAMOND_PICKAXE));
        open();
        tapInventory(0);
        assertTrue(BedrockAnvilGUI.tryOpen(other, null));
        BedrockAnvilGUI otherGui = BedrockAnvilGUI.of(other);
        assertNotNull(otherGui);
        // Tap through the other player's own view.
        other.getInventory().setItem(0, new ItemStack(Material.DIAMOND_PICKAXE));
        otherGui.onInventoryClick(new InventoryClickEvent(other.getOpenInventory(), SlotType.CONTAINER, raw(0), ClickType.LEFT, InventoryAction.PICKUP_ALL));
        assertNotNull(otherGui.getInventory().getItem(SLOT_ITEM));

        BedrockAnvilGUI.closeAll();

        assertNull(BedrockAnvilGUI.of(player));
        assertNull(BedrockAnvilGUI.of(other));
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
        assertEquals(Material.DIAMOND_PICKAXE, other.getInventory().getItem(0).getType());
    }

    @Test
    void givenMenuReopened_thenFirstMenuInputsAreReturnedNotLost() {
        give(0, sword("sharpness", 5));
        open();
        tapInventory(0);

        open();

        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertNull(menu().getItem(SLOT_ITEM), "the new menu is empty; the sword is back in the inventory");
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
    }

    // ----------------------------------------------------------------------------------------------------
    // Preview

    @Test
    void givenSwordAndBook_thenPreviewShowsBookWithCostAndModeIsDisenchant() {
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        open();

        tapInventory(0);
        tapInventory(1);

        ItemStack preview = menu().getItem(SLOT_RESULT);
        assertEquals(Material.ENCHANTED_BOOK, preview.getType());
        assertTrue(preview.getItemMeta().hasLore(), "the preview lists what transfers and the cost");
        assertEquals(Material.EMERALD, menu().getItem(SLOT_CONFIRM).getType());
        assertTrue(PreviewLore.hasPreview(preview));
        assertNotNull(menu().getItem(SLOT_MODE).getItemMeta().displayName());
    }

    @Test
    void givenNothingEligible_thenResultShowsNotice() {
        give(0, new ItemStack(Material.DIAMOND_SWORD));
        give(1, new ItemStack(Material.BOOK));
        open();

        tapInventory(0);
        tapInventory(1);

        assertNotEquals(Material.ENCHANTED_BOOK, menu().getItem(SLOT_RESULT).getType());
        assertEquals(Material.GRAY_DYE, menu().getItem(SLOT_CONFIRM).getType());
    }

    // ----------------------------------------------------------------------------------------------------
    // Disenchant

    private void putSwordAndBook(ItemStack sword) {
        give(0, sword);
        give(1, new ItemStack(Material.BOOK, 2));
        open();
        tapInventory(0);
        tapInventory(1);
        tapInventory(1);
    }

    @Test
    void givenDisenchant_whenConfirmed_thenBookDeliveredSwordStrippedBookConsumedAndLevelsCharged() {
        putSwordAndBook(sword("sharpness", 5, "unbreaking", 3));

        tapMenu(SLOT_CONFIRM);

        ItemStack delivered = firstOf(Material.ENCHANTED_BOOK);
        assertNotNull(delivered, "the book goes to the inventory");
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) delivered.getItemMeta();
        assertEquals(5, meta.getStoredEnchantLevel(enchantment("sharpness")));
        assertEquals(3, meta.getStoredEnchantLevel(enchantment("unbreaking")));

        ItemStack source = menu().getItem(SLOT_ITEM);
        assertEquals(Material.DIAMOND_SWORD, source.getType());
        assertTrue(source.getEnchantments().isEmpty(), "enchantments were moved, not copied");

        assertEquals(1, menu().getItem(SLOT_SECOND).getAmount(), "one of the two blank books was used");
        assertTrue(player.getLevel() < 30, "levels were charged");
        assertEquals(1, disenchanted.get());
    }

    @Test
    void givenDisenchant_whenConfirmed_thenDeliveredBookCarriesNoPreviewLore() {
        putSwordAndBook(sword("sharpness", 5));
        assertTrue(menu().getItem(SLOT_RESULT).getItemMeta().hasLore(), "precondition: the preview has lore");

        tapMenu(SLOT_CONFIRM);

        ItemStack delivered = firstOf(Material.ENCHANTED_BOOK);
        assertNotNull(delivered);
        assertFalse(delivered.hasItemMeta() && delivered.getItemMeta().hasLore(), "preview lore must not leak");
        assertFalse(PreviewLore.hasPreview(delivered));
    }

    @Test
    void givenDoubleClickOnConfirm_thenTheOperationRunsOnce() {
        putSwordAndBook(sword("sharpness", 5));
        int levelBefore = player.getLevel();

        tapMenu(SLOT_CONFIRM);
        int levelAfterFirst = player.getLevel();
        tapMenu(SLOT_CONFIRM);
        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get());
        assertEquals(levelAfterFirst, player.getLevel(), "no second charge");
        assertTrue(levelAfterFirst < levelBefore);
        assertEquals(1, count(Material.ENCHANTED_BOOK), "exactly one book was produced");
    }

    @Test
    void givenConfirmAfterTheSourceIsSpent_thenNothingMoreHappens() {
        putSwordAndBook(sword("sharpness", 5));
        tapMenu(SLOT_CONFIRM);
        advanceClock();
        int level = player.getLevel();

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get(), "the stripped sword has nothing left to give");
        assertEquals(level, player.getLevel());
        assertEquals(1, count(Material.ENCHANTED_BOOK));
    }

    @Test
    void givenReentrantConfirmFromAPreListener_thenNoNestedOperation() {
        putSwordAndBook(sword("sharpness", 5));
        AtomicInteger pre = new AtomicInteger();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPre(PreDisenchantEvent e) {
                pre.incrementAndGet();
                tapMenu(SLOT_CONFIRM);
            }
        }, plugin);

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, pre.get(), "the nested confirm was ignored");
        assertEquals(1, disenchanted.get());
        assertEquals(1, count(Material.ENCHANTED_BOOK));
    }

    @Test
    void givenNotEnoughLevels_thenNothingChangesAndNothingIsCharged() {
        player.setLevel(0);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, player.getLevel());
        assertEquals(0, disenchanted.get());
        assertEquals(5, menu().getItem(SLOT_ITEM).getEnchantmentLevel(enchantment("sharpness")), "the sword keeps its enchantment");
        assertEquals(2, menu().getItem(SLOT_SECOND).getAmount());
        assertEquals(0, count(Material.ENCHANTED_BOOK));
    }

    @Test
    void givenCreative_thenNoLevelsAreChargedButTheOperationRuns() {
        player.setGameMode(GameMode.CREATIVE);
        player.setLevel(0);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get());
        assertEquals(0, player.getLevel());
    }

    @Test
    void givenPermissionRevoked_thenRefusedAndNothingChanges() {
        for (String node : List.of("disenchantment.anvil.all", "disenchantment.anvil.disenchant", "disenchantment.anvil", "disenchantment.anvil.item")) {
            player.addAttachment(plugin, node, false);
        }
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
        assertEquals(5, menu().getItem(SLOT_ITEM).getEnchantmentLevel(enchantment("sharpness")));
    }

    @Test
    void givenFeatureDisabled_thenRefused() {
        setConfig("disenchantment.enabled", false);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
    }

    @Test
    void givenWorldDisabled_thenRefused() {
        setConfig("disenchantment.worlds.mode", "DENYLIST");
        setConfig("disenchantment.worlds.list", List.of(player.getWorld().getName()));
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
    }

    @Test
    void givenDisabledEnchantmentState_thenRefused() {
        setDisenchantEnchantmentStates(List.of("sharpness:DISABLE"));
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
    }

    @Test
    void givenMaintenance_thenRefusedUnlessBypass() {
        Disenchantment.maintenanceEnabled = true;
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
    }

    @Test
    void givenCooldown_thenSecondOperationRefusedUntilItEnds() {
        setConfig("disenchantment.cooldown.seconds", 600);
        putSwordAndBook(sword("sharpness", 5));
        tapMenu(SLOT_CONFIRM);
        assertEquals(1, disenchanted.get());

        // Fresh inputs: another enchanted sword; the first confirm started the cooldown.
        tapMenu(SLOT_ITEM);
        give(2, sword("unbreaking", 3));
        tapInventory(2);
        advanceClock();
        int level = player.getLevel();

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get(), "on cooldown");
        assertEquals(level, player.getLevel());
        assertEquals(3, menu().getItem(SLOT_ITEM).getEnchantmentLevel(enchantment("unbreaking")));
    }

    @Test
    void givenTooExpensive_thenRefusedUnlessBypassIsOn() {
        setConfig("disenchantment.anvil.repair.base", 60);
        putSwordAndBook(sword("sharpness", 5));
        player.setLevel(100);

        tapMenu(SLOT_CONFIRM);
        assertEquals(0, disenchanted.get(), "40 or more levels is 'Too Expensive!' like at the anvil");

        setConfig("disenchantment.anvil.bypass-too-expensive", true);
        advanceClock();
        tapMenu(SLOT_CONFIRM);
        assertEquals(1, disenchanted.get());
    }

    @Test
    void givenPreEventCancelled_thenNothingIsConsumedOrCharged() {
        putSwordAndBook(sword("sharpness", 5));
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPre(PreDisenchantEvent e) {
                e.setCancelled(true);
            }
        }, plugin);

        tapMenu(SLOT_CONFIRM);

        assertEquals(30, player.getLevel());
        assertEquals(0, disenchanted.get());
        assertEquals(2, menu().getItem(SLOT_SECOND).getAmount());
        assertEquals(5, menu().getItem(SLOT_ITEM).getEnchantmentLevel(enchantment("sharpness")));
    }

    @Test
    void givenFullInventory_whenConfirmed_thenTheBookIsDroppedNotLost() {
        putSwordAndBook(sword("sharpness", 5));
        fillInventoryWith(Material.DIRT);

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get());
        assertEquals(1, count(Material.ENCHANTED_BOOK));
        assertNull(firstOf(Material.ENCHANTED_BOOK), "it is on the floor");
    }

    // ----------------------------------------------------------------------------------------------------
    // Economy

    @Test
    void givenEconomy_whenConfirmed_thenWithdrawnOnce() {
        Economy economy = EconomyTestSupport.install();
        setConfig("disenchantment.economy.enabled", true);
        setConfig("disenchantment.economy.cost", 100.0);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);
        tapMenu(SLOT_CONFIRM);

        Mockito.verify(economy, Mockito.times(1)).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.eq(100.0));
        assertEquals(1, disenchanted.get());
    }

    @Test
    void givenInsufficientFunds_thenNothingHappensAndNothingIsTaken() {
        Economy economy = EconomyTestSupport.install();
        Mockito.when(economy.has(Mockito.any(OfflinePlayer.class), Mockito.anyDouble())).thenReturn(false);
        setConfig("disenchantment.economy.enabled", true);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        Mockito.verify(economy, Mockito.never()).withdrawPlayer(Mockito.any(OfflinePlayer.class), Mockito.anyDouble());
        assertEquals(30, player.getLevel());
        assertEquals(0, disenchanted.get());
        assertEquals(5, menu().getItem(SLOT_ITEM).getEnchantmentLevel(enchantment("sharpness")));
    }

    @Test
    void givenEconomyEnabledButNoProvider_thenRefused() {
        setConfig("disenchantment.economy.enabled", true);
        putSwordAndBook(sword("sharpness", 5));

        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
    }

    // ----------------------------------------------------------------------------------------------------
    // Shatter

    @Test
    void givenEnchantedBookAndBlankBook_whenConfirmed_thenOneEnchantmentIsSplitOff() {
        give(0, enchantedBook("sharpness", "unbreaking", "efficiency"));
        give(1, new ItemStack(Material.BOOK));
        open();
        tapInventory(0);
        tapInventory(1);

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, shattered.get());
        ItemStack delivered = firstOf(Material.ENCHANTED_BOOK);
        assertNotNull(delivered);
        assertEquals(1, ((EnchantmentStorageMeta) delivered.getItemMeta()).getStoredEnchants().size());
        EnchantmentStorageMeta source = (EnchantmentStorageMeta) menu().getItem(SLOT_ITEM).getItemMeta();
        assertEquals(2, source.getStoredEnchants().size(), "the source keeps the other two");
        assertNull(menu().getItem(SLOT_SECOND), "the blank book was used");
    }

    @Test
    void givenShatter_thenThePreviewedEnchantmentIsTheOneDelivered() {
        give(0, enchantedBook("sharpness", "unbreaking", "efficiency", "mending"));
        give(1, new ItemStack(Material.BOOK));
        open();
        tapInventory(0);
        tapInventory(1);
        EnchantmentStorageMeta previewed = (EnchantmentStorageMeta) menu().getItem(SLOT_RESULT).getItemMeta();

        tapMenu(SLOT_CONFIRM);

        EnchantmentStorageMeta delivered = (EnchantmentStorageMeta) firstOf(Material.ENCHANTED_BOOK).getItemMeta();
        assertEquals(previewed.getStoredEnchants(), delivered.getStoredEnchants());
    }

    // ----------------------------------------------------------------------------------------------------
    // Erase

    @Test
    void givenErase_whenConfirmed_thenItemReturnsWithoutEnchantmentsAndCatalystIsConsumed() {
        setConfig("erase.enabled", true);
        give(0, sword("sharpness", 5, "unbreaking", 3));
        give(1, new ItemStack(Material.ECHO_SHARD, 2));
        open();
        tapInventory(0);
        tapInventory(1);
        assertEquals(Material.ECHO_SHARD, menu().getItem(SLOT_SECOND).getType());

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, erased.get());
        ItemStack delivered = firstOf(Material.DIAMOND_SWORD);
        assertNotNull(delivered);
        assertTrue(delivered.getEnchantments().isEmpty());
        assertNull(menu().getItem(SLOT_ITEM), "the item itself was handed over as the result");
        assertNull(menu().getItem(SLOT_SECOND), "the single catalyst was used");
        assertEquals(1, count(Material.ECHO_SHARD), "the second shard stayed in the inventory");
        assertTrue(player.getLevel() < 30);
    }

    @Test
    void givenEraseWithConsumeOff_thenCatalystStays() {
        setConfig("erase.enabled", true);
        setConfig("erase.consume-catalyst", false);
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.ECHO_SHARD));
        open();
        tapInventory(0);
        tapInventory(1);

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, erased.get());
        assertEquals(Material.ECHO_SHARD, menu().getItem(SLOT_SECOND).getType());
    }

    // ----------------------------------------------------------------------------------------------------
    // Who may open it

    @Test
    void givenFallbackDisabled_thenMenuDoesNotOpen() {
        setConfig("bedrock.fallback-enabled", false);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));

        assertNull(BedrockAnvilGUI.of(player));
        assertNotNull(player.nextMessage());
    }

    @Test
    void givenJavaPlayer_thenBlockedUnlessAllowed() {
        PlayerMock java = server.addPlayer("Alex");

        assertFalse(BedrockAnvilGUI.tryOpen(java, null));
        assertNull(BedrockAnvilGUI.of(java));

        setConfig("bedrock.allow-java-players", true);

        assertTrue(BedrockAnvilGUI.tryOpen(java, null));
        assertNotNull(BedrockAnvilGUI.of(java));
    }

    @Test
    void givenFloodgatePlayerWithPlainName_thenAllowedViaFloodgate() {
        PlayerMock floodgate = server.addPlayer("Plain");
        org.geysermc.floodgate.api.FloodgateApi.BEDROCK.add(floodgate.getUniqueId());
        try {
            assertTrue(BedrockAnvilGUI.tryOpen(floodgate, null));
        } finally {
            org.geysermc.floodgate.api.FloodgateApi.reset();
        }
    }

    @Test
    void givenPluginDisabledInConfig_thenMenuDoesNotOpen() {
        setConfig("enabled", false);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));
    }

    // ----------------------------------------------------------------------------------------------------
    // Special anvils (#62 must not be bypassable): the player must look at one, and be allowed to use it

    private org.bukkit.block.Block lookingAt;

    private org.bukkit.block.Block anvilAt(int x, int y, int z, boolean marked) {
        var block = world().getBlockAt(x, y, z);
        block.setType(Material.ANVIL);
        if (marked) com.jankominek.disenchantment.anvils.SpecialAnvilStore.mark(block);
        return block;
    }

    private void lookAt(org.bukkit.block.Block block) {
        lookingAt = block;
        BedrockAnvilGUI.setTargetResolver(p -> lookingAt);
    }

    @AfterEach
    void resetResolver() {
        BedrockAnvilGUI.setTargetResolver(null);
    }

    @Test
    void givenAllMode_thenCommandWorksAnywhereWithoutLookingAtAnything() {
        BedrockAnvilGUI.setTargetResolver(p -> null);

        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
    }

    @Test
    void givenMarkedMode_whenNotLookingAtAnything_thenRefusedEvenNextToAMarkedAnvil() {
        setConfig("special-anvils.mode", "MARKED");
        anvilAt(2, 64, 2, true);
        lookAt(null);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null), "being near a special anvil is not enough");

        assertNull(BedrockAnvilGUI.of(player));
        assertEquals(org.bukkit.ChatColor.stripColor(I18n.getPrefix() + " " + I18n.Messages.specialAnvilRequired()), org.bukkit.ChatColor.stripColor(player.nextMessage()));
    }

    @Test
    void givenMarkedMode_whenLookingAtANonAnvilBlock_thenRefused() {
        setConfig("special-anvils.mode", "MARKED");
        anvilAt(2, 64, 2, true);
        var stone = world().getBlockAt(3, 64, 0);
        stone.setType(Material.STONE);
        lookAt(stone);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));
        assertNull(BedrockAnvilGUI.of(player));
    }

    @Test
    void givenMarkedMode_whenLookingAtAPlainAnvil_thenRefused() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, false));

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));
    }

    @Test
    void givenMarkedMode_whenLookingAtAMarkedAnvil_thenOpens() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));

        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
    }

    @Test
    void givenBlockBelowMode_thenTheLookedAtAnvilMustStandOnTheBlock() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        setConfig("special-anvils.block-below", "LAPIS_BLOCK");
        world().getBlockAt(2, 63, 2).setType(Material.LAPIS_BLOCK);
        var good = anvilAt(2, 64, 2, false);
        world().getBlockAt(-2, 63, 2).setType(Material.STONE);
        var bad = anvilAt(-2, 64, 2, false);

        lookAt(bad);
        assertFalse(BedrockAnvilGUI.tryOpen(player, null));
        lookAt(good);
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
    }

    @Test
    void givenAProtectionPluginCancelsTheInteract_thenRefusedAndNothingOpens() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        AtomicInteger probes = new AtomicInteger();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(org.bukkit.event.player.PlayerInteractEvent e) {
                probes.incrementAndGet();
                e.setCancelled(true);
            }
        }, plugin);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));

        assertEquals(1, probes.get(), "the access probe is a right click on that anvil");
        assertNull(BedrockAnvilGUI.of(player));
    }

    @Test
    void givenAProtectionPluginDeniesBlockUse_thenRefused() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(org.bukkit.event.player.PlayerInteractEvent e) {
                e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            }
        }, plugin);

        assertFalse(BedrockAnvilGUI.tryOpen(player, null));
    }

    @Test
    void givenSneakModeAndSneaking_thenTheProbeIsNotMistakenForARealSneakClick() {
        setConfig("special-anvils.mode", "MARKED");
        setConfig("bedrock.open-on-sneak-click", true);
        player.setSneaking(true);
        lookAt(anvilAt(3, 64, 3, true));
        AtomicInteger menusOpenedDuringProbe = new AtomicInteger();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
            public void onInteract(org.bukkit.event.player.PlayerInteractEvent e) {
                if (BedrockAnvilGUI.of(player) != null) menusOpenedDuringProbe.incrementAndGet();
            }
        }, plugin);

        assertTrue(BedrockAnvilGUI.tryOpen(player, null));

        assertEquals(0, menusOpenedDuringProbe.get(), "our own listener ignores the synthetic event");
    }

    @Test
    void givenSpecialAnvilBrokenAfterOpening_thenNextClickClosesAndReturnsItems() {
        setConfig("special-anvils.mode", "MARKED");
        var block = anvilAt(3, 64, 3, true);
        lookAt(block);
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        tapInventory(0);
        tapInventory(1);

        block.setType(Material.AIR);
        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertEquals(30, player.getLevel());
        assertNull(BedrockAnvilGUI.of(player), "the menu was closed");
        assertEquals(1, count(Material.DIAMOND_SWORD));
        assertEquals(1, count(Material.BOOK));
        assertNotNull(firstOf(Material.DIAMOND_SWORD), "returned to the inventory");
        assertEquals(5, firstOf(Material.DIAMOND_SWORD).getEnchantmentLevel(enchantment("sharpness")));
    }

    @Test
    void givenSpecialAnvilUnmarkedAfterOpening_thenConfirmIsRefusedAndItemsReturned() {
        setConfig("special-anvils.mode", "MARKED");
        var block = anvilAt(3, 64, 3, true);
        lookAt(block);
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        tapInventory(0);
        tapInventory(1);

        com.jankominek.disenchantment.anvils.SpecialAnvilStore.unmark(block);
        tapMenu(SLOT_CONFIRM);

        assertEquals(0, disenchanted.get());
        assertNull(BedrockAnvilGUI.of(player));
        assertNotNull(firstOf(Material.DIAMOND_SWORD));
        assertNotNull(firstOf(Material.BOOK));
    }

    // MockBukkit closes the open inventory on teleport (the real server does not), so the distance and world
    // rules are checked on the requirement itself instead of through a click after the teleport.
    @Test
    void givenPlayerWalksAwayFromTheAnvil_thenTheRequirementNoLongerHolds() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);
        assertTrue(gui.specialAnvilHolds());

        player.teleport(new Location(world(), 40.5, 64, 40.5));

        assertFalse(gui.specialAnvilHolds());
    }

    @Test
    void givenPlayerStaysWithinSixBlocks_thenTheRequirementHolds() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);

        player.teleport(new Location(world(), 3.5, 64, 8.5));
        assertTrue(gui.specialAnvilHolds(), "5 blocks away");

        player.teleport(new Location(world(), 3.5, 64, 11.5));
        assertFalse(gui.specialAnvilHolds(), "8 blocks away");
    }

    @Test
    void givenPlayerChangesWorld_thenTheRequirementNoLongerHolds() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);

        player.teleport(new Location(server.addSimpleWorld("other"), 3.5, 65, 3.5));

        assertFalse(gui.specialAnvilHolds());
    }

    @Test
    void givenModeSwitchedToAllWhileOpen_thenNothingIsRequiredAnymore() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);

        setConfig("special-anvils.mode", "ALL");

        assertTrue(gui.specialAnvilHolds());
    }

    @Test
    void givenSpecialAnvilStillThereWithinReach_thenConfirmWorks() {
        setConfig("special-anvils.mode", "MARKED");
        lookAt(anvilAt(3, 64, 3, true));
        give(0, sword("sharpness", 5));
        give(1, new ItemStack(Material.BOOK));
        assertTrue(BedrockAnvilGUI.tryOpen(player, null));
        tapInventory(0);
        tapInventory(1);

        tapMenu(SLOT_CONFIRM);

        assertEquals(1, disenchanted.get());
    }

    @Test
    void givenSneakClickedAnvil_thenThatAnvilMustBeSpecial() {
        setConfig("special-anvils.mode", "MARKED");
        var plain = anvilAt(3, 64, 3, false);
        var special = anvilAt(-3, 64, 3, true);

        assertFalse(BedrockAnvilGUI.tryOpen(player, plain));
        assertTrue(BedrockAnvilGUI.tryOpen(player, special));
    }

    // ----------------------------------------------------------------------------------------------------
    // Listing / sanity

    // ----------------------------------------------------------------------------------------------------
    // Conservation under arbitrary click sequences (no loss, no duplication)

    private int enchantmentLevelsEverywhere() {
        int total = 0;
        List<ItemStack> all = new ArrayList<>(java.util.Arrays.asList(player.getInventory().getContents()));
        BedrockAnvilGUI gui = BedrockAnvilGUI.of(player);
        if (gui != null) {
            all.add(gui.getInventory().getItem(SLOT_ITEM));
            all.add(gui.getInventory().getItem(SLOT_SECOND));
        }
        for (Item dropped : player.getWorld().getEntitiesByClass(Item.class)) all.add(dropped.getItemStack());

        for (ItemStack stack : all) {
            if (stack == null || stack.getType().isAir()) continue;
            for (int level : stack.getEnchantments().values()) total += level;
            if (stack.getItemMeta() instanceof EnchantmentStorageMeta storage) {
                for (int level : storage.getStoredEnchants().values()) total += level;
            }
        }
        return total;
    }

    @Test
    void givenRandomClickSequences_thenNoItemOrEnchantmentIsEverLostOrDuplicated() {
        for (long seed = 1; seed <= 30; seed++) {
            player.getInventory().clear();
            for (Item dropped : new ArrayList<>(player.getWorld().getEntitiesByClass(Item.class))) dropped.remove();
            BedrockAnvilGUI.closeAll();
            player.setLevel(1000);
            setConfig("disenchantment.anvil.bypass-too-expensive", true);

            give(0, sword("sharpness", 5));
            give(1, sword("unbreaking", 3));
            give(2, new ItemStack(Material.BOOK, 4));
            give(3, new ItemStack(Material.DIAMOND_PICKAXE));
            int levelsBefore = enchantmentLevelsEverywhere();
            assertEquals(8, levelsBefore);

            java.util.Random random = new java.util.Random(seed);
            int lastLevel = player.getLevel();

            for (int step = 0; step < 250; step++) {
                if (BedrockAnvilGUI.of(player) == null) open();

                switch (random.nextInt(8)) {
                    // Mostly the slots that hold something, sometimes any slot.
                    case 0, 1, 2 -> tapInventory(random.nextBoolean() ? random.nextInt(4) : random.nextInt(36));
                    case 3 -> tapMenu(SLOT_ITEM);
                    case 4 -> tapMenu(SLOT_SECOND);
                    case 5, 6 -> tapMenu(SLOT_CONFIRM);
                    default -> {
                        if (random.nextBoolean()) tapMenu(SLOT_CANCEL);
                        else player.closeInventory();
                    }
                }
                if (random.nextInt(3) == 0) advanceClock();

                String context = "seed " + seed + ", step " + step;
                assertEquals(2, count(Material.DIAMOND_SWORD), context + ": swords");
                assertEquals(1, count(Material.DIAMOND_PICKAXE), context + ": pickaxe");
                assertEquals(4, count(Material.BOOK) + count(Material.ENCHANTED_BOOK), context + ": books");
                assertEquals(levelsBefore, enchantmentLevelsEverywhere(), context + ": enchantment levels");
                assertTrue(player.getLevel() <= lastLevel && player.getLevel() >= 0, context + ": levels only ever go down");
                lastLevel = player.getLevel();
            }

            BedrockAnvilGUI.closeAll();
            assertEquals(2, count(Material.DIAMOND_SWORD), "seed " + seed + ": after closing");
            assertEquals(levelsBefore, enchantmentLevelsEverywhere(), "seed " + seed + ": after closing");
        }

        assertTrue(disenchanted.get() > 10, "the random walk must actually run operations, it ran " + disenchanted.get());
    }

    @Test
    void givenSeveralOperations_thenTotalBooksAndEnchantmentsAreConserved() {
        // Two enchanted swords and three blank books: after both disenchants no enchantment is lost or doubled.
        give(0, sword("sharpness", 5));
        give(1, sword("unbreaking", 3));
        give(2, new ItemStack(Material.BOOK, 3));
        open();
        tapInventory(0);
        tapInventory(2);
        tapMenu(SLOT_CONFIRM);
        tapMenu(SLOT_ITEM);
        advanceClock();
        tapInventory(2);
        tapInventory(1);
        tapMenu(SLOT_CONFIRM);
        BedrockAnvilGUI.of(player).finish();

        List<ItemStack> books = new ArrayList<>();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.ENCHANTED_BOOK) books.add(stack);
        }
        assertEquals(2, books.size());
        int totalLevels = 0;
        for (ItemStack book : books) {
            for (int level : ((EnchantmentStorageMeta) book.getItemMeta()).getStoredEnchants().values()) totalLevels += level;
        }
        assertEquals(5 + 3, totalLevels);
        assertEquals(1, count(Material.BOOK), "three books minus two used");
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.DIAMOND_SWORD) assertTrue(stack.getEnchantments().isEmpty());
        }
    }
}
