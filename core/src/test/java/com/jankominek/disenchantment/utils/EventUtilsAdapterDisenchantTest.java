package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the adapter-aware disenchant path:
 * {@code EventUtils.Disenchantment.getDisenchantedEnchantments(item, book, withDelete, plugin, world)}.
 * Uses MockPluginAdapter so no real third-party plugin JARs are needed.
 */
class EventUtilsAdapterDisenchantTest extends DisenchantmentTestBase {

    // -> helpers

    private World world() { return server.getWorlds().get(0); }

    private MockPluginAdapter adapter(IPluginEnchantment... enchants) {
        return new MockPluginAdapter("TestPlugin", enchants);
    }

    // -> basic acceptance / rejection

    @Test
    void givenAdapterEnchantedSword_whenGetDisenchanted_thenReturnsAdapterEnchantments() {
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertEquals(1, result.size());
        assertEquals("sharpness", result.get(0).getKey());
        assertEquals(5, result.get(0).getLevel());
    }

    @Test
    void givenNullFirstItem_whenGetDisenchanted_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        null, new ItemStack(Material.BOOK), false, adapter(), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullSecondItem_whenGetDisenchanted_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), null,
                        false, adapter(mockEnchant("sharpness", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenEnchantedBookAsFirstItem_whenGetDisenchanted_thenReturnsEmpty() {
        // Adapter path still rejects ENCHANTED_BOOK as first item (shattering only)
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.BOOK),
                        false, adapter(mockEnchant("sharpness", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemNotBook_whenGetDisenchanted_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.STICK),
                        false, adapter(mockEnchant("sharpness", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemIsEnchantedBook_whenGetDisenchanted_thenReturnsEmpty() {
        // Adapter returns enchantments for ENCHANTED_BOOK → slot1 not blank → rejected
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.ENCHANTED_BOOK),
                        false, adapter(mockEnchant("mending", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenAdapterReturnsNoEnchantments_whenGetDisenchanted_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter(), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenMultipleEnchantments_whenGetDisenchanted_thenAllReturned() {
        MockPluginAdapter adapter = adapter(
                mockEnchant("sharpness", 5),
                mockEnchant("unbreaking", 3),
                mockEnchant("looting", 3));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertEquals(3, result.size());
    }

    // -> disabled material

    @Test
    void givenDisabledMaterial_whenGetDisenchanted_thenReturnsEmpty() {
        setConfig("disenchantment.disabled-materials", List.of("DIAMOND_SWORD"));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter(mockEnchant("sharpness", 1)), world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenOtherMaterialDisabled_whenGetDisenchanted_thenStillReturns() {
        setConfig("disenchantment.disabled-materials", List.of("IRON_SWORD"));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter(mockEnchant("sharpness", 1)), world());
        assertFalse(result.isEmpty());
    }

    // -> enchantment state: KEEP

    @Test
    void givenKEEPState_whenGetDisenchanted_thenEnchantmentFiltered() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertTrue(result.stream().noneMatch(e -> e.getKey().equals("sharpness")));
        assertEquals(1, result.size());
        assertEquals("mending", result.get(0).getKey());
    }

    @Test
    void givenAllEnchantmentsInKEEP_whenGetDisenchanted_thenResultEmpty() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep", "mending:keep"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertTrue(result.isEmpty());
    }

    // -> enchantment state: DISABLE

    @Test
    void givenDISABLEState_whenGetDisenchanted_thenEntireOperationBlocked() {
        setDisenchantEnchantmentStates(List.of("mending:disable"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertTrue(result.isEmpty());
    }

    @Test
    void givenDISABLEOnAbsentEnchantment_whenGetDisenchanted_thenProceedsNormally() {
        setDisenchantEnchantmentStates(List.of("mending:disable"));
        // Item does NOT have mending from adapter
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("efficiency", 3));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertEquals(2, result.size());
    }

    // -> enchantment state: DELETE

    @Test
    void givenDELETEState_withDeleteFalse_thenDeletedEnchantIncluded() {
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        false, adapter, world());
        assertEquals(2, result.size());
    }

    @Test
    void givenDELETEState_withDeleteTrue_thenDeletedEnchantFiltered() {
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        MockPluginAdapter adapter = adapter(mockEnchant("sharpness", 5), mockEnchant("mending", 1));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.BOOK),
                        true, adapter, world());
        assertEquals(1, result.size());
        assertEquals("sharpness", result.get(0).getKey());
    }

    // -> findEnchantmentsToDelete

    @Test
    void givenMixedStates_whenFindToDelete_thenOnlyDELETEReturned() {
        setDisenchantEnchantmentStates(List.of("mending:delete", "sharpness:keep"));
        List<IPluginEnchantment> enchantments = List.of(
                mockEnchant("sharpness", 1),
                mockEnchant("mending", 1),
                mockEnchant("efficiency", 3));
        List<IPluginEnchantment> toDelete =
                EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments);
        assertEquals(1, toDelete.size());
        assertEquals("mending", toDelete.get(0).getKey());
    }
}
