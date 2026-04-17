package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventUtilsShatterTest extends DisenchantmentTestBase {

    // ── helpers ──────────────────────────────────────────────────────────────

    private ItemStack blankBook() {
        return new ItemStack(Material.BOOK);
    }

    private ItemStack enchantedBook(String... enchantKeys) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        for (String key : enchantKeys) EnchantmentUtils.addStoredEnchantment(item, enchantment(key), 1);
        return item;
    }

    // ── basic acceptance / rejection ─────────────────────────────────────────

    @Test
    void givenBookWithTwoEnchantments_whenGetShatterEnchantments_thenReturnsBoth() {
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertEquals(2, result.size());
    }

    @Test
    void givenBookWithOneEnchantment_whenGetShatterEnchantments_thenReturnsEmpty() {
        // Shattering requires at least 2 enchantments
        ItemStack book = enchantedBook("sharpness");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullFirstItem_whenGetShatterEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(null, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullSecondItem_whenGetShatterEnchantments_thenReturnsEmpty() {
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, null, false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNonEnchantedBookAsFirstItem_whenGetShatterEnchantments_thenReturnsEmpty() {
        // First slot must be ENCHANTED_BOOK, not a plain book
        ItemStack plainBook = new ItemStack(Material.BOOK);
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(plainBook, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSwordAsFirstItem_whenGetShatterEnchantments_thenReturnsEmpty() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(sword, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemNotBlankBook_whenGetShatterEnchantments_thenReturnsEmpty() {
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, new ItemStack(Material.STICK), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemIsEnchantedBook_whenGetShatterEnchantments_thenReturnsEmpty() {
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, enchantedBook("efficiency"), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenBookWithThreeEnchantments_whenGetShatterEnchantments_thenReturnsAll() {
        ItemStack book = enchantedBook("sharpness", "mending", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertEquals(3, result.size());
    }

    // ── enchantment state: KEEP ───────────────────────────────────────────────

    @Test
    void givenEnchantmentInKEEPState_whenGetShatterEnchantments_thenEnchantmentFiltered() {
        setShatterEnchantmentStates(List.of("mending:keep"));
        ItemStack book = enchantedBook("sharpness", "mending", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertTrue(result.stream().noneMatch(e -> e.getKey().equals("mending")));
        assertEquals(2, result.size());
    }

    @Test
    void givenKEEPFilterReducesToOne_whenGetShatterEnchantments_thenReturnsFiltered() {
        setShatterEnchantmentStates(List.of("mending:keep"));
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        // sharpness passes through, mending filtered — 1 result returned
        assertEquals(1, result.size());
        assertEquals("sharpness", result.get(0).getKey());
    }

    // ── enchantment state: DISABLE ────────────────────────────────────────────

    @Test
    void givenEnchantmentInDISABLEState_whenGetShatterEnchantments_thenEntireOperationBlocked() {
        setShatterEnchantmentStates(List.of("mending:disable"));
        ItemStack book = enchantedBook("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    // ── enchantment state: DELETE ─────────────────────────────────────────────

    @Test
    void givenEnchantmentInDELETEState_withDeleteFalse_thenDeletedEnchantIncluded() {
        // withDelete=false (used by click event): DELETE enchants ARE included so that
        // findEnchantmentsToDelete can identify and remove them from the source item.
        setShatterEnchantmentStates(List.of("mending:delete"));
        ItemStack book = enchantedBook("sharpness", "mending", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(book, blankBook(), false);
        assertEquals(3, result.size());
    }

    // ── findEnchantmentsToDelete ──────────────────────────────────────────────

    @Test
    void givenMixedStates_whenShatterFindToDelete_thenOnlyDELETEReturned() {
        setShatterEnchantmentStates(List.of("mending:delete", "sharpness:keep"));
        List<IPluginEnchantment> enchantments = List.of(
                EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 1),
                EnchantmentUtils.remapEnchantment(enchantment("mending"), 1),
                EnchantmentUtils.remapEnchantment(enchantment("efficiency"), 3)
        );
        List<IPluginEnchantment> toDelete =
                EventUtils.Shatterment.findEnchantmentsToDelete(enchantments);
        assertEquals(1, toDelete.size());
        assertEquals("mending", toDelete.get(0).getKey());
    }
}
