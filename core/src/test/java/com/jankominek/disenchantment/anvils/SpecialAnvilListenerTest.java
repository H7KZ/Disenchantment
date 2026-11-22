package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lifecycle tests: placing, breaking, dropping, falling and using special anvils. Events are
 * dispatched through the plugin manager so the registered {@link SpecialAnvilListener} receives them.
 */
class SpecialAnvilListenerTest extends DisenchantmentTestBase {

    private PlayerMock player;
    private Block block;

    @BeforeEach
    void setUp() {
        player = server.addPlayer("Builder");
        block = world().getBlockAt(5, 64, 5);
    }

    // -> helpers

    private void place(Block target, ItemStack inHand, PlayerMock who) {
        target.setType(inHand.getType());
        BlockState replaced = target.getState();
        server.getPluginManager().callEvent(new BlockPlaceEvent(target, replaced, target.getRelative(0, -1, 0), inHand, who, true));
    }

    private BlockBreakEvent breakBlock(Block target, PlayerMock who) {
        BlockBreakEvent event = new BlockBreakEvent(target, who);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private List<Item> dropItems(Block target, PlayerMock who, ItemStack... stacks) {
        BlockState state = target.getState();
        List<Item> items = new ArrayList<>();
        for (ItemStack stack : stacks) items.add(world().dropItem(target.getLocation(), stack));
        server.getPluginManager().callEvent(new BlockDropItemEvent(target, state, who, items));
        return items;
    }

    // -> placement

    @Test
    void givenSpecialItem_whenPlaced_thenAnvilMarked() {
        place(block, SpecialAnvilItem.create(1), player);

        assertTrue(SpecialAnvilStore.isMarked(block));
    }

    @Test
    void givenPlainAnvilItem_whenPlaced_thenNotMarked() {
        place(block, new ItemStack(Material.ANVIL), player);

        assertFalse(SpecialAnvilStore.isMarked(block));
    }

    @Test
    void givenStaleMark_whenPlainAnvilPlacedOverIt_thenMarkCleared() {
        SpecialAnvilStore.mark(block);

        place(block, new ItemStack(Material.ANVIL), player);

        assertFalse(SpecialAnvilStore.isMarked(block), "regular anvil must not inherit a stale mark");
        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenPlayerWithoutSpecialPermission_whenPlacingSpecialItem_thenPlacedAsNormalAnvil() {
        player.addAttachment(plugin, "disenchantment.anvil.special", false);

        place(block, SpecialAnvilItem.create(1), player);

        assertEquals(Material.ANVIL, block.getType());
        assertFalse(SpecialAnvilStore.isMarked(block));
    }

    @Test
    void givenCancelledPlacement_whenPlacingSpecialItem_thenNotMarked() {
        block.setType(Material.ANVIL);
        BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0),
                SpecialAnvilItem.create(1), player, true);
        event.setCancelled(true);

        server.getPluginManager().callEvent(event);

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenNonAnvilBlock_whenPlacedFromSpecialItemStack_thenNothingRecorded() {
        block.setType(Material.STONE);
        server.getPluginManager().callEvent(new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0),
                SpecialAnvilItem.create(1), player, true));

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    // -> break

    @Test
    void givenMarkedAnvil_whenBroken_thenUnmarked() {
        place(block, SpecialAnvilItem.create(1), player);

        breakBlock(block, player);

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenCancelledBreak_whenMarkedAnvil_thenMarkKept() {
        place(block, SpecialAnvilItem.create(1), player);
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        event.setCancelled(true);

        server.getPluginManager().callEvent(event);

        assertTrue(SpecialAnvilStore.isMarked(block));
    }

    @Test
    void givenUnmarkedAnvil_whenBroken_thenNothingHappens() {
        place(block, new ItemStack(Material.ANVIL), player);

        breakBlock(block, player);

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    // -> drops

    @Test
    void givenMarkedAnvilBroken_whenDropsFire_thenAnvilStackSwappedForSpecialItem() {
        place(block, SpecialAnvilItem.create(1), player);
        breakBlock(block, player);

        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL));

        assertTrue(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
        assertEquals(1, items.get(0).getItemStack().getAmount());
    }

    @Test
    void givenDamagedMarkedAnvilBroken_whenDropsFire_thenDamageStateKept() {
        place(block, SpecialAnvilItem.create(1), player);
        block.setType(Material.DAMAGED_ANVIL);
        breakBlock(block, player);

        List<Item> items = dropItems(block, player, new ItemStack(Material.DAMAGED_ANVIL));

        ItemStack drop = items.get(0).getItemStack();
        assertEquals(Material.DAMAGED_ANVIL, drop.getType());
        assertTrue(SpecialAnvilItem.isSpecial(drop));
    }

    @Test
    void givenDropSpecialItemDisabled_whenMarkedAnvilBroken_thenPlainDropAndMarkRemoved() {
        setConfig("special-anvils.drop-special-item", false);
        place(block, SpecialAnvilItem.create(1), player);
        breakBlock(block, player);

        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL));

        assertFalse(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenUnmarkedAnvilBroken_whenDropsFire_thenDropUntouched() {
        place(block, new ItemStack(Material.ANVIL), player);
        breakBlock(block, player);

        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL));

        assertFalse(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
    }

    @Test
    void givenMarkedAnvilBroken_whenOtherItemsDrop_thenOnlyAnvilStacksSwapped() {
        place(block, SpecialAnvilItem.create(1), player);
        breakBlock(block, player);

        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL), new ItemStack(Material.STICK));

        assertTrue(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
        assertEquals(Material.STICK, items.get(1).getItemStack().getType());
        assertFalse(SpecialAnvilItem.isSpecial(items.get(1).getItemStack()));
    }

    @Test
    void givenCreativeBreak_whenMarkedAnvil_thenMarkRemovedAndNoDropSwapArmed() {
        player.setGameMode(GameMode.CREATIVE);
        place(block, SpecialAnvilItem.create(1), player);
        breakBlock(block, player);

        // A later, unrelated anvil drop at the same position must stay a plain anvil.
        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL));

        assertFalse(SpecialAnvilStore.hasRawMark(block));
        assertFalse(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
    }

    @Test
    void givenDropWithoutPrecedingMarkedBreak_whenDropsFire_thenNotSwapped() {
        List<Item> items = dropItems(block, player, new ItemStack(Material.ANVIL));

        assertFalse(SpecialAnvilItem.isSpecial(items.get(0).getItemStack()));
    }

    // -> gravity

    private FallingBlock falling(Location at) {
        return world().spawnFallingBlock(at, Material.ANVIL.createBlockData());
    }

    @Test
    void givenMarkedAnvil_whenItStartsFalling_thenUnmarkedAndEntityTagged() {
        place(block, SpecialAnvilItem.create(1), player);
        FallingBlock fb = falling(block.getLocation());

        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, block, Material.AIR.createBlockData()));

        assertFalse(SpecialAnvilStore.hasRawMark(block));
        assertTrue(fb.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "special_fall"), PersistentDataType.BYTE));
    }

    @Test
    void givenTaggedFallingAnvil_whenItLands_thenLandingPositionMarked() {
        place(block, SpecialAnvilItem.create(1), player);
        FallingBlock fb = falling(block.getLocation());
        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, block, Material.AIR.createBlockData()));
        block.setType(Material.AIR);

        Block landing = world().getBlockAt(5, 60, 5);
        landing.setType(Material.AIR);
        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, landing, Material.ANVIL.createBlockData()));
        landing.setType(Material.ANVIL);

        assertTrue(SpecialAnvilStore.isMarked(landing));
    }

    @Test
    void givenUntaggedFallingAnvil_whenItLandsOnStaleMark_thenMarkCleared() {
        Block landing = world().getBlockAt(7, 60, 7);
        SpecialAnvilStore.mark(landing);
        FallingBlock fb = falling(landing.getLocation());

        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, landing, Material.ANVIL.createBlockData()));
        landing.setType(Material.ANVIL);

        assertFalse(SpecialAnvilStore.isMarked(landing));
    }

    @Test
    void givenUnmarkedAnvil_whenItStartsFalling_thenEntityNotTagged() {
        block.setType(Material.ANVIL);
        FallingBlock fb = falling(block.getLocation());

        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, block, Material.AIR.createBlockData()));

        assertFalse(fb.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "special_fall"), PersistentDataType.BYTE));
    }

    @Test
    void givenTaggedFallingAnvil_whenItBreaksIntoItem_thenSpecialItemDropped() {
        place(block, SpecialAnvilItem.create(1), player);
        FallingBlock fb = falling(block.getLocation());
        server.getPluginManager().callEvent(new EntityChangeBlockEvent(fb, block, Material.AIR.createBlockData()));
        Item drop = world().dropItem(block.getLocation(), new ItemStack(Material.ANVIL));

        server.getPluginManager().callEvent(new EntityDropItemEvent(fb, drop));

        assertTrue(SpecialAnvilItem.isSpecial(drop.getItemStack()));
    }

    @Test
    void givenUntaggedFallingAnvil_whenItBreaksIntoItem_thenDropUntouched() {
        FallingBlock fb = falling(block.getLocation());
        Item drop = world().dropItem(block.getLocation(), new ItemStack(Material.ANVIL));

        server.getPluginManager().callEvent(new EntityDropItemEvent(fb, drop));

        assertFalse(SpecialAnvilItem.isSpecial(drop.getItemStack()));
    }

    // -> use / destruction

    private InventoryCloseEvent closeEvent(Location location) {
        AnvilInventory inv = Mockito.mock(AnvilInventory.class);
        Mockito.when(inv.getType()).thenReturn(InventoryType.ANVIL);
        Mockito.when(inv.getLocation()).thenReturn(location);
        InventoryCloseEvent event = Mockito.mock(InventoryCloseEvent.class);
        Mockito.when(event.getInventory()).thenReturn(inv);
        return event;
    }

    @Test
    void givenAnvilDestroyedByUse_whenInventoryClosed_thenMarkPruned() {
        place(block, SpecialAnvilItem.create(1), player);
        block.setType(Material.AIR);

        new SpecialAnvilListener().onClose(closeEvent(block.getLocation()));

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenAnvilDestroyedAfterClose_whenNextTickRuns_thenMarkPruned() {
        place(block, SpecialAnvilItem.create(1), player);

        new SpecialAnvilListener().onClose(closeEvent(block.getLocation()));
        assertTrue(SpecialAnvilStore.hasRawMark(block), "still an anvil right after close");

        block.setType(Material.AIR);
        server.getScheduler().performOneTick();

        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenSurvivingAnvil_whenInventoryClosed_thenMarkKept() {
        place(block, SpecialAnvilItem.create(1), player);
        block.setType(Material.CHIPPED_ANVIL);

        new SpecialAnvilListener().onClose(closeEvent(block.getLocation()));
        server.getScheduler().performOneTick();

        assertTrue(SpecialAnvilStore.isMarked(block), "degrading to a chipped anvil keeps the mark");
    }

    @Test
    void givenVirtualAnvil_whenInventoryClosed_thenNothingHappens() {
        assertDoesNotThrow(() -> new SpecialAnvilListener().onClose(closeEvent(null)));
    }
}
