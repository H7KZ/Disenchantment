package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventUtilsDisenchantTest extends DisenchantmentTestBase {

    // -> helpers

    private ItemStack sword(String... enchantKeys) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        for (String key : enchantKeys) item.addUnsafeEnchantment(enchantment(key), 1);
        return item;
    }

    private ItemStack swordWithLevel(String enchantKey, int level) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        item.addUnsafeEnchantment(enchantment(enchantKey), level);
        return item;
    }

    private ItemStack blankBook() {
        return new ItemStack(Material.BOOK);
    }

    private ItemStack enchantedBook(String... enchantKeys) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        for (String key : enchantKeys) EnchantmentUtils.addStoredEnchantment(item, enchantment(key), 1);
        return item;
    }

    // -> basic acceptance / rejection

    @Test
    void givenEnchantedSwordAndBlankBook_whenGetEnchantments_thenReturnsEnchantments() {
        ItemStack sword = sword("sharpness");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertFalse(result.isEmpty());
        assertEquals("sharpness", result.get(0).getKey());
    }

    @Test
    void givenNullFirstItem_whenGetEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(null, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullSecondItem_whenGetEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword("sharpness"), null, false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenAirFirstItem_whenGetEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.AIR), blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenEnchantedBookAsFirstItem_whenGetEnchantments_thenReturnsEmpty() {
        // Enchanted books can only be shattered, not disenchanted
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        enchantedBook("sharpness"), blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemNotBook_whenGetEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        sword("sharpness"), new ItemStack(Material.STICK), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSecondItemIsEnchantedBook_whenGetEnchantments_thenReturnsEmpty() {
        // Second slot must be a blank BOOK, not an enchanted book
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        sword("sharpness"), enchantedBook("mending"), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenItemWithNoEnchantments_whenGetEnchantments_thenReturnsEmpty() {
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        new ItemStack(Material.DIAMOND_SWORD), blankBook(), false);
        assertTrue(result.isEmpty());
    }

    // -> disabled material

    @Test
    void givenDisabledMaterial_whenGetEnchantments_thenReturnsEmpty() {
        setConfig("disenchantment.disabled-materials", List.of("DIAMOND_SWORD"));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        sword("sharpness"), blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenOtherMaterialDisabled_whenGetEnchantments_thenStillReturnsEnchantments() {
        setConfig("disenchantment.disabled-materials", List.of("IRON_SWORD"));
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(
                        sword("sharpness"), blankBook(), false);
        assertFalse(result.isEmpty());
    }

    // -> enchantment state: KEEP

    @Test
    void givenEnchantmentInKEEPState_whenGetEnchantments_thenEnchantmentFiltered() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));
        ItemStack sword = sword("sharpness", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertTrue(result.stream().noneMatch(e -> e.getKey().equals("sharpness")));
        assertTrue(result.stream().anyMatch(e -> e.getKey().equals("efficiency")));
    }

    @Test
    void givenAllEnchantmentsInKEEPState_whenGetEnchantments_thenResultEmpty() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep", "efficiency:keep"));
        ItemStack sword = sword("sharpness", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    // -> enchantment state: DISABLE

    @Test
    void givenEnchantmentInDISABLEState_whenGetEnchantments_thenEntireOperationBlocked() {
        // DISABLE means the whole disenchant is rejected, not just that enchantment
        setDisenchantEnchantmentStates(List.of("mending:disable"));
        ItemStack sword = sword("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenDISABLEOnAbsentEnchantment_whenGetEnchantments_thenProceedsNormally() {
        setDisenchantEnchantmentStates(List.of("mending:disable"));
        // Item does NOT have mending — DISABLE only blocks if the enchant is present
        ItemStack sword = sword("sharpness", "efficiency");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertEquals(2, result.size());
    }

    // -> enchantment state: DELETE

    @Test
    void givenEnchantmentInDELETEState_withDeleteFalse_thenDeletedEnchantIncluded() {
        // withDelete=false (used by click event): DELETE enchants ARE included so that
        // findEnchantmentsToDelete can identify and remove them from the source item.
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        ItemStack sword = sword("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertEquals(2, result.size());
    }

    @Test
    void givenEnchantmentInDELETEState_withDeleteTrue_thenDeletedEnchantAlsoFiltered() {
        // withDelete=true: DELETE enchants also removed (they'll be deleted from item)
        setDisenchantEnchantmentStates(List.of("mending:delete"));
        ItemStack sword = sword("sharpness", "mending");
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), true);
        assertEquals(1, result.size());
        assertEquals("sharpness", result.get(0).getKey());
    }

    // -> findEnchantmentsToDelete

    @Test
    void givenMixedStates_whenFindToDelete_thenOnlyDELETEReturned() {
        setDisenchantEnchantmentStates(List.of("mending:delete", "sharpness:keep"));
        List<IPluginEnchantment> enchantments = List.of(
                EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 1),
                EnchantmentUtils.remapEnchantment(enchantment("mending"), 1),
                EnchantmentUtils.remapEnchantment(enchantment("efficiency"), 3)
        );
        List<IPluginEnchantment> toDelete =
                EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments);
        assertEquals(1, toDelete.size());
        assertEquals("mending", toDelete.get(0).getKey());
    }

    @Test
    void givenNoDeleteStates_whenFindToDelete_thenReturnsEmpty() {
        List<IPluginEnchantment> enchantments = List.of(
                EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5)
        );
        List<IPluginEnchantment> toDelete =
                EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments);
        assertTrue(toDelete.isEmpty());
    }

    // -> multiple enchantments preserved

    @Test
    void givenMultipleEnchantments_whenGetEnchantments_thenAllPreserved() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(enchantment("sharpness"), 5);
        sword.addUnsafeEnchantment(enchantment("efficiency"), 3);
        sword.addUnsafeEnchantment(enchantment("unbreaking"), 3);
        List<IPluginEnchantment> result =
                EventUtils.Disenchantment.getDisenchantedEnchantments(sword, blankBook(), false);
        assertEquals(3, result.size());
    }
}
