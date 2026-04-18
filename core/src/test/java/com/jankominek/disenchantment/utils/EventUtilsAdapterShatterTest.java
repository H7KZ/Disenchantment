package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the adapter-aware shatter path:
 * {@code EventUtils.Shatterment.getShattermentEnchantments(item, book, withDelete, plugin, world)}.
 */
class EventUtilsAdapterShatterTest extends DisenchantmentTestBase {

    // -> helpers

    private MockPluginAdapter adapter(IPluginEnchantment... enchants) {
        return new MockPluginAdapter("TestPlugin", enchants);
    }

    private ItemStack enchantedBook() { return new ItemStack(Material.ENCHANTED_BOOK); }

    private ItemStack blankBook() { return new ItemStack(Material.BOOK); }

    // -> basic acceptance / rejection

    @Test
    void givenBookWithTwoAdapterEnchantments_whenGetShatter_thenReturnsBoth() {
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertEquals(2, result.size());
    }

    @Test
    void givenBookWithOneAdapterEnchantment_whenGetShatter_thenReturnsEmpty() {
        // Shatter requires ≥2 enchantments
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullFirstItem_whenGetShatter_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        null, blankBook(), false, adapter(), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullSecondItem_whenGetShatter_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), null, false,
                        adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNonEnchantedBookAsFirstItem_whenGetShatter_thenReturnsEmpty() {
        // First slot must be ENCHANTED_BOOK
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        blankBook(), blankBook(), false,
                        adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemNotBlankBook_whenGetShatter_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), new ItemStack(Material.STICK), false,
                        adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemIsEnchantedBook_whenGetShatter_thenReturnsEmpty() {
        // Adapter returns enchantments for ENCHANTED_BOOK → slot1 not blank
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), enchantedBook(), false,
                        adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenBookWithThreeEnchantments_whenGetShatter_thenReturnsAll() {
        MockPluginAdapter adapter = adapter(
                mockEnchant("sharpness", 1), mockEnchant("mending", 1), mockEnchant("efficiency", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertEquals(3, result.size());
    }

    // -> enchantment state: KEEP

    @Test
    void givenKEEPState_whenGetShatter_thenEnchantmentFiltered() {
        setShatterEnchantmentStates(List.of("mending:keep"));
        MockPluginAdapter adapter = adapter(
                mockEnchant("sharpness", 1), mockEnchant("mending", 1), mockEnchant("efficiency", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertTrue(result.stream().noneMatch(e -> e.getKey().equals("mending")));
        assertEquals(2, result.size());
    }

    // -> enchantment state: DISABLE

    @Test
    void givenDISABLEState_whenGetShatter_thenEntireOperationBlocked() {
        setShatterEnchantmentStates(List.of("mending:disable"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 1), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertTrue(result.isEmpty());
    }

    // -> enchantment state: DELETE

    @Test
    void givenDELETEState_withDeleteFalse_thenDeletedEnchantIncluded() {
        setShatterEnchantmentStates(List.of("mending:delete"));
        MockPluginAdapter adapter = adapter(
                mockEnchant("sharpness", 1), mockEnchant("mending", 1), mockEnchant("efficiency", 1));
        List<IPluginEnchantment> result =
                EventUtils.Shatterment.getShattermentEnchantments(
                        enchantedBook(), blankBook(), false, adapter, world());
        assertEquals(3, result.size());
    }

    // -> findEnchantmentsToDelete

    @Test
    void givenMixedStates_whenShatterFindToDelete_thenOnlyDELETEReturned() {
        setShatterEnchantmentStates(List.of("mending:delete", "sharpness:keep"));
        List<IPluginEnchantment> enchantments = List.of(
                mockEnchant("sharpness", 1),
                mockEnchant("mending", 1),
                mockEnchant("efficiency", 3));
        List<IPluginEnchantment> toDelete =
                EventUtils.Shatterment.findEnchantmentsToDelete(enchantments);
        assertEquals(1, toDelete.size());
        assertEquals("mending", toDelete.get(0).getKey());
    }
}
