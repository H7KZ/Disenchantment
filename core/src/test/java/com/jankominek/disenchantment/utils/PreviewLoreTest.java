package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the preview-lore marker: appended lines are counted in PDC and stripped again, exactly and idempotently.
 */
class PreviewLoreTest extends DisenchantmentTestBase {

    private ItemStack book(List<String> lore) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        if (lore != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Integer markerOf(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(plugin, "preview_lines"), PersistentDataType.INTEGER);
    }

    @Test
    void givenLines_whenAppend_thenLoreExtendedAndCounted() {
        ItemStack item = book(List.of("custom"));

        PreviewLore.append(item, List.of("a", "b"));

        assertEquals(List.of("custom", "a", "b"), item.getItemMeta().getLore());
        assertEquals(2, markerOf(item));
        assertTrue(PreviewLore.hasPreview(item));
    }

    @Test
    void givenEmptyOrNull_whenAppend_thenUntouched() {
        ItemStack item = book(null);

        PreviewLore.append(item, List.of());
        PreviewLore.append(item, null);
        PreviewLore.append(null, List.of("x"));

        assertFalse(item.getItemMeta().hasLore());
        assertNull(markerOf(item));
    }

    @Test
    void givenTwoAppends_whenStrip_thenAllPreviewLinesRemoved() {
        ItemStack item = book(List.of("custom"));
        PreviewLore.append(item, List.of("a"));
        PreviewLore.append(item, List.of("b", "c"));
        assertEquals(3, markerOf(item));

        PreviewLore.strip(item);

        assertEquals(List.of("custom"), item.getItemMeta().getLore());
        assertNull(markerOf(item));
    }

    @Test
    void givenOnlyPreviewLines_whenStrip_thenLoreIsNullNotEmpty() {
        ItemStack item = book(null);
        PreviewLore.append(item, List.of("a", "b"));

        PreviewLore.strip(item);

        assertFalse(item.getItemMeta().hasLore());
        assertNull(item.getItemMeta().getLore());
        assertNull(markerOf(item));
    }

    @Test
    void givenStrippedItem_whenStripAgain_thenIdempotent() {
        ItemStack item = book(List.of("custom"));
        PreviewLore.append(item, List.of("a"));

        PreviewLore.strip(item);
        PreviewLore.strip(item);

        assertEquals(List.of("custom"), item.getItemMeta().getLore());
        assertFalse(PreviewLore.hasPreview(item));
    }

    @Test
    void givenItemWithoutMarker_whenStrip_thenOtherLoreUntouched() {
        ItemStack item = book(List.of("x", "y"));

        PreviewLore.strip(item);

        assertEquals(List.of("x", "y"), item.getItemMeta().getLore());
    }

    @Test
    void givenMarkerLargerThanLore_whenStrip_thenLoreRemovedWithoutError() {
        ItemStack item = book(null);
        PreviewLore.append(item, List.of("a", "b", "c"));
        ItemMeta meta = item.getItemMeta();
        meta.setLore(List.of("only-one-left"));
        item.setItemMeta(meta);

        PreviewLore.strip(item);

        assertFalse(item.getItemMeta().hasLore());
        assertNull(markerOf(item));
    }

    @Test
    void givenNullOrMetalessItem_whenStrip_thenNoError() {
        assertNull(PreviewLore.strip(null));
        ItemStack plain = new ItemStack(Material.BOOK);
        assertSame(plain, PreviewLore.strip(plain));
        assertFalse(PreviewLore.hasPreview(null));
    }
}
