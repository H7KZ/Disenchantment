package com.jankominek.disenchantment.anvils;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Remembers which placed anvils are special. Anvils are not tile entities, so the marks live in the
 * chunk's {@link PersistentDataContainer} as one {@code long[]} of packed chunk-local positions
 * under {@code disenchantment:special_anvils}. The array is tiny, so a linear lookup is cheap
 * even though {@code PrepareAnvilEvent} fires on every keystroke.
 *
 * <p>Marks are position based: an anvil changing damage state keeps its mark. A mark whose block is
 * no longer an anvil (broken by use, {@code /setblock}, WorldEdit, ...) is ignored and pruned the
 * next time it is read.</p>
 */
public class SpecialAnvilStore {
    /**
     * Lowest supported block Y; positions below it cannot be stored.
     */
    public static final int MIN_Y = -2048;

    /**
     * Highest supported block Y; positions above it cannot be stored.
     */
    public static final int MAX_Y = 2047;

    private static final String KEY_NAME = "special_anvils";

    private SpecialAnvilStore() {
    }

    private static NamespacedKey key() {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    // ----------------------------------------------------------------------------------------------------
    // Packing

    /**
     * Packs a block position into a chunk-local value: {@code (x&15) | (z&15)<<4 | (y+2048)<<8}.
     * World coordinates are fine, only the low four bits of x and z are used.
     *
     * @param x block x
     * @param y block y, within {@value #MIN_Y}..{@value #MAX_Y}
     * @param z block z
     * @return the packed position, or {@code -1} when {@code y} is out of range
     */
    public static long pack(int x, int y, int z) {
        if (y < MIN_Y || y > MAX_Y) return -1L;

        return (x & 15) | ((long) (z & 15) << 4) | ((long) (y + 2048) << 8);
    }

    /**
     * Extracts the chunk-local x (0-15) from a packed position.
     */
    public static int unpackX(long packed) {
        return (int) (packed & 15);
    }

    /**
     * Extracts the chunk-local z (0-15) from a packed position.
     */
    public static int unpackZ(long packed) {
        return (int) ((packed >> 4) & 15);
    }

    /**
     * Extracts the block y from a packed position.
     */
    public static int unpackY(long packed) {
        return (int) (packed >> 8) - 2048;
    }

    // ----------------------------------------------------------------------------------------------------
    // Material

    /**
     * Returns whether the material is one of the three anvil damage states.
     *
     * @param material the material to test (may be {@code null})
     * @return {@code true} for {@code ANVIL}, {@code CHIPPED_ANVIL} and {@code DAMAGED_ANVIL}
     */
    public static boolean isAnvilMaterial(Material material) {
        return material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL;
    }

    // ----------------------------------------------------------------------------------------------------
    // Marks

    /**
     * Returns whether the block is a marked special anvil. A mark on a block that is no longer an
     * anvil is removed and reported as not marked.
     *
     * @param block the block to test
     * @return {@code true} if the block is an anvil that carries a mark
     */
    public static boolean isMarked(Block block) {
        if (block == null) return false;

        long packed = pack(block.getX(), block.getY(), block.getZ());
        if (packed < 0) return false;

        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        long[] marks = pdc.get(key(), PersistentDataType.LONG_ARRAY);
        if (marks == null || !contains(marks, packed)) return false;

        if (!isAnvilMaterial(block.getType())) {
            write(pdc, remove(marks, packed));
            return false;
        }

        return true;
    }

    /**
     * Returns whether a mark exists at the position, without checking the block type or pruning.
     *
     * @param block the block to test
     * @return {@code true} if a raw mark is stored for the position
     */
    public static boolean hasRawMark(Block block) {
        long packed = pack(block.getX(), block.getY(), block.getZ());
        if (packed < 0) return false;

        long[] marks = block.getChunk().getPersistentDataContainer().get(key(), PersistentDataType.LONG_ARRAY);
        return marks != null && contains(marks, packed);
    }

    /**
     * Marks the block's position as a special anvil. Does not check the block type, so it may be
     * called just before a landing falling anvil becomes a block.
     *
     * @param block the block to mark
     */
    public static void mark(Block block) {
        long packed = pack(block.getX(), block.getY(), block.getZ());
        if (packed < 0) return;

        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        long[] marks = pdc.get(key(), PersistentDataType.LONG_ARRAY);
        if (marks == null) {
            write(pdc, new long[]{packed});
        } else if (!contains(marks, packed)) {
            long[] grown = Arrays.copyOf(marks, marks.length + 1);
            grown[marks.length] = packed;
            write(pdc, grown);
        }
    }

    /**
     * Removes the mark at the block's position, if any.
     *
     * @param block the block to unmark
     */
    public static void unmark(Block block) {
        long packed = pack(block.getX(), block.getY(), block.getZ());
        if (packed < 0) return;

        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        long[] marks = pdc.get(key(), PersistentDataType.LONG_ARRAY);
        if (marks != null && contains(marks, packed)) write(pdc, remove(marks, packed));
    }

    /**
     * Removes the mark at the block's position when the block is no longer an anvil.
     *
     * @param block the block to check
     * @return {@code true} if a stale mark was removed
     */
    public static boolean pruneIfStale(Block block) {
        if (isAnvilMaterial(block.getType()) || !hasRawMark(block)) return false;

        unmark(block);
        return true;
    }

    /**
     * Returns how many raw marks are stored in the chunk (diagnostics and tests).
     *
     * @param chunk the chunk to inspect
     * @return the number of stored marks
     */
    public static int countMarks(Chunk chunk) {
        long[] marks = chunk.getPersistentDataContainer().get(key(), PersistentDataType.LONG_ARRAY);
        return marks == null ? 0 : marks.length;
    }

    // ----------------------------------------------------------------------------------------------------
    // Internals

    private static boolean contains(long[] marks, long packed) {
        for (long mark : marks) if (mark == packed) return true;
        return false;
    }

    private static long[] remove(long[] marks, long packed) {
        return Arrays.stream(marks).filter(mark -> mark != packed).toArray();
    }

    // An emptied array removes the key entirely so chunks without special anvils carry no data.
    private static void write(PersistentDataContainer pdc, long[] marks) {
        if (marks.length == 0) {
            pdc.remove(key());
        } else {
            pdc.set(key(), PersistentDataType.LONG_ARRAY, marks);
        }
    }
}
