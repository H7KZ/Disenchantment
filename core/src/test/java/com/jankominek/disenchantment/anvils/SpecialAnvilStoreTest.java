package com.jankominek.disenchantment.anvils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the chunk-PDC special anvil store and the special anvil item.
 */
class SpecialAnvilStoreTest extends DisenchantmentTestBase {

    private Block anvilAt(int x, int y, int z) {
        Block block = world().getBlockAt(x, y, z);
        block.setType(Material.ANVIL);
        return block;
    }

    // -> packing

    @Test
    void givenPositions_whenPackedAndUnpacked_thenLocalCoordinatesRoundTrip() {
        int[][] positions = {{0, 0, 0}, {15, 64, 15}, {16, 64, 16}, {-1, 64, -1}, {-17, -64, -33}, {123456, 319, -98765}};

        for (int[] p : positions) {
            long packed = SpecialAnvilStore.pack(p[0], p[1], p[2]);

            assertTrue(packed >= 0, "valid positions pack to a non-negative value");
            assertEquals(p[0] & 15, SpecialAnvilStore.unpackX(packed));
            assertEquals(p[1], SpecialAnvilStore.unpackY(packed));
            assertEquals(p[2] & 15, SpecialAnvilStore.unpackZ(packed));
        }
    }

    @Test
    void givenYRangeEdges_whenPacked_thenAcceptedOrRejected() {
        assertEquals(SpecialAnvilStore.MIN_Y, SpecialAnvilStore.unpackY(SpecialAnvilStore.pack(0, SpecialAnvilStore.MIN_Y, 0)));
        assertEquals(SpecialAnvilStore.MAX_Y, SpecialAnvilStore.unpackY(SpecialAnvilStore.pack(0, SpecialAnvilStore.MAX_Y, 0)));
        assertEquals(-1L, SpecialAnvilStore.pack(0, SpecialAnvilStore.MIN_Y - 1, 0));
        assertEquals(-1L, SpecialAnvilStore.pack(0, SpecialAnvilStore.MAX_Y + 1, 0));
    }

    @Test
    void givenSameLocalPositionInDifferentChunks_whenPacked_thenEqual() {
        assertEquals(SpecialAnvilStore.pack(3, 70, 5), SpecialAnvilStore.pack(3 + 16, 70, 5 - 32));
        assertNotEquals(SpecialAnvilStore.pack(3, 70, 5), SpecialAnvilStore.pack(3, 71, 5));
    }

    // -> mark / unmark

    @Test
    void givenAnvil_whenMarked_thenIsMarked() {
        Block block = anvilAt(2, 64, 3);

        assertFalse(SpecialAnvilStore.isMarked(block));
        SpecialAnvilStore.mark(block);

        assertTrue(SpecialAnvilStore.isMarked(block));
    }

    @Test
    void givenMarkedAnvil_whenUnmarked_thenNotMarkedAndKeyRemoved() {
        Block block = anvilAt(2, 64, 3);
        SpecialAnvilStore.mark(block);

        SpecialAnvilStore.unmark(block);

        assertFalse(SpecialAnvilStore.isMarked(block));
        assertEquals(0, SpecialAnvilStore.countMarks(block.getChunk()));
    }

    @Test
    void givenMarkTwice_whenCounted_thenStoredOnce() {
        Block block = anvilAt(2, 64, 3);

        SpecialAnvilStore.mark(block);
        SpecialAnvilStore.mark(block);

        assertEquals(1, SpecialAnvilStore.countMarks(block.getChunk()));
    }

    @Test
    void givenMarksAcrossChunkBordersAndNegativeCoordinates_whenQueried_thenEachIsIndependent() {
        Block a = anvilAt(15, 64, 15);
        Block b = anvilAt(16, 64, 15);
        Block c = anvilAt(-1, 10, -1);
        Block d = anvilAt(-16, 10, -16);
        Block plain = anvilAt(5, 64, 5);

        SpecialAnvilStore.mark(a);
        SpecialAnvilStore.mark(c);

        assertTrue(SpecialAnvilStore.isMarked(a));
        assertFalse(SpecialAnvilStore.isMarked(b), "next chunk over has no mark");
        assertTrue(SpecialAnvilStore.isMarked(c));
        assertFalse(SpecialAnvilStore.isMarked(d), "different chunk, same local corner family");
        assertFalse(SpecialAnvilStore.isMarked(plain));
    }

    @Test
    void givenSeveralMarksInOneChunk_whenOneUnmarked_thenOthersRemain() {
        Block a = anvilAt(1, 64, 1);
        Block b = anvilAt(2, 64, 2);
        SpecialAnvilStore.mark(a);
        SpecialAnvilStore.mark(b);

        SpecialAnvilStore.unmark(a);

        assertFalse(SpecialAnvilStore.isMarked(a));
        assertTrue(SpecialAnvilStore.isMarked(b));
    }

    @Test
    void givenAllAnvilStates_whenMarked_thenAllCountAsMarked() {
        for (Material state : new Material[]{Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL}) {
            Block block = world().getBlockAt(7, 64, 7);
            block.setType(state);
            SpecialAnvilStore.mark(block);

            assertTrue(SpecialAnvilStore.isMarked(block), state + " keeps the mark");
        }
    }

    // -> stale marks

    @Test
    void givenMarkedAnvilBecameAir_whenRead_thenPrunedAndNotMarked() {
        Block block = anvilAt(4, 64, 4);
        SpecialAnvilStore.mark(block);
        block.setType(Material.AIR);

        assertFalse(SpecialAnvilStore.isMarked(block));
        assertEquals(0, SpecialAnvilStore.countMarks(block.getChunk()), "stale mark was pruned on read");
    }

    @Test
    void givenStaleMark_whenRegularAnvilAppearsOnPosition_thenPrunedMarkDoesNotLeak() {
        Block block = anvilAt(4, 64, 4);
        SpecialAnvilStore.mark(block);
        block.setType(Material.AIR);
        assertFalse(SpecialAnvilStore.isMarked(block));

        block.setType(Material.ANVIL);

        assertFalse(SpecialAnvilStore.isMarked(block), "a new regular anvil must not inherit the old mark");
    }

    @Test
    void givenStaleMark_whenPruneIfStale_thenRemovedOnlyWhenNotAnvil() {
        Block block = anvilAt(4, 64, 4);
        SpecialAnvilStore.mark(block);

        assertFalse(SpecialAnvilStore.pruneIfStale(block), "still an anvil");
        assertTrue(SpecialAnvilStore.hasRawMark(block));

        block.setType(Material.STONE);

        assertTrue(SpecialAnvilStore.pruneIfStale(block));
        assertFalse(SpecialAnvilStore.hasRawMark(block));
    }

    @Test
    void givenNullBlock_whenQueried_thenFalse() {
        assertFalse(SpecialAnvilStore.isMarked(null));
    }

    // -> item

    @Test
    void givenSpecialItem_whenCreated_thenCarriesMarkerNameAndLore() {
        ItemStack item = SpecialAnvilItem.create(3);

        assertEquals(Material.ANVIL, item.getType());
        assertEquals(3, item.getAmount());
        assertTrue(SpecialAnvilItem.isSpecial(item));

        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertEquals("§dEnchanter's Anvil", meta.getDisplayName());
        assertEquals(java.util.List.of("§7Place to create a disenchanting anvil"), meta.getLore());
    }

    @Test
    void givenSpecialItemSurvivesClone_whenChecked_thenStillSpecial() {
        ItemStack copy = SpecialAnvilItem.create(1).clone();

        assertTrue(SpecialAnvilItem.isSpecial(copy));
    }

    @Test
    void givenRenamedSpecialItem_whenChecked_thenStillSpecialByMarkerOnly() {
        ItemStack item = SpecialAnvilItem.create(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Totally a normal anvil");
        item.setItemMeta(meta);

        assertTrue(SpecialAnvilItem.isSpecial(item));
    }

    @Test
    void givenPlainOrLookalikeAnvil_whenChecked_thenNotSpecial() {
        ItemStack lookalike = new ItemStack(Material.ANVIL);
        ItemMeta meta = lookalike.getItemMeta();
        meta.setDisplayName("§dEnchanter's Anvil");
        lookalike.setItemMeta(meta);

        assertFalse(SpecialAnvilItem.isSpecial(new ItemStack(Material.ANVIL)));
        assertFalse(SpecialAnvilItem.isSpecial(lookalike));
        assertFalse(SpecialAnvilItem.isSpecial(null));
        assertFalse(SpecialAnvilItem.isSpecial(new ItemStack(Material.STONE)));
    }

    @Test
    void givenCustomNameAndEmptyLore_whenCreated_thenReflected() {
        setConfig("special-anvils.item.name", "&6Gold");
        setConfig("special-anvils.item.lore", java.util.List.of());

        ItemMeta meta = SpecialAnvilItem.create(1).getItemMeta();

        assertEquals("§6Gold", meta.getDisplayName());
        assertFalse(meta.hasLore());
    }

    @Test
    void givenDamagedState_whenCreated_thenKeepsStateAndMarker() {
        ItemStack chipped = SpecialAnvilItem.create(Material.CHIPPED_ANVIL, 1);

        assertEquals(Material.CHIPPED_ANVIL, chipped.getType());
        assertTrue(SpecialAnvilItem.isSpecial(chipped));
        assertEquals(Material.ANVIL, SpecialAnvilItem.create(Material.STONE, 1).getType(), "non-anvil falls back to ANVIL");
    }
}
