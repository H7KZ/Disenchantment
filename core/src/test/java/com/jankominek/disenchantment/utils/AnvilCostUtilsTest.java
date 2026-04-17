package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnvilCostUtilsTest extends DisenchantmentTestBase {

    // Default config values: base=10, multiply=0.25, cost=true

    @Test
    void givenNoEnchantments_whenCountCost_thenReturnsBaseCost() {
        int cost = AnvilCostUtils.countAnvilCost(List.of(), AnvilEventType.DISENCHANTMENT);
        assertEquals(10, cost); // base=10, no enchantments
    }

    @Test
    void givenNoEnchantments_shatterment_whenCountCost_thenReturnsBaseCost() {
        int cost = AnvilCostUtils.countAnvilCost(List.of(), AnvilEventType.SHATTERMENT);
        assertEquals(10, cost);
    }

    @Test
    void givenCostDisabled_whenCountCost_thenReturnsZero() {
        setConfig("disenchantment.anvil.repair.cost", false);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(0, cost);
    }

    @Test
    void givenSharpnessV_defaultConfig_whenCountCost_thenCorrectTotal() {
        // base=10, multiplier=0.25, enchantment level=5
        // cost = 10 + (5 * 0.25) = 11.25 → rounded = 11
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(11, cost);
    }

    @Test
    void givenTwoEnchantments_whenCountCost_thenHigherLevelComputedFirst() {
        // Sort descending by level: Sharpness V (5), Mending I (1)
        // base=10, multiplier=0.25
        // cost = 10 + (5 * 0.25) + (1 * 0.50) = 11.75 → rounded = 12
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        IPluginEnchantment mendingI = EnchantmentUtils.remapEnchantment(enchantment("mending"), 1);
        int cost = AnvilCostUtils.countAnvilCost(List.of(mendingI, sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(12, cost);
    }

    @ParameterizedTest
    @CsvSource({
        "0.0, 0.0,  0",   // base=0, multiplier=0 → always 0
        "5.0, 0.0,  5",   // base=5, multiplier=0 → just base
        "0.0, 1.0,  3",   // base=0, multiplier=1 → 3*1 (level 3 enchant)
        "10.0, 1.0, 13"   // base=10, multiplier=1 → 10 + 3*1
    })
    void givenCustomBaseAndMultiplier_whenCountCost_thenMatchesFormula(
            double base, double multiply, int expected) {
        setConfig("disenchantment.anvil.repair.base", base);
        setConfig("disenchantment.anvil.repair.multiply", multiply);
        IPluginEnchantment enchant = EnchantmentUtils.remapEnchantment(enchantment("efficiency"), 3);
        int cost = AnvilCostUtils.countAnvilCost(List.of(enchant), AnvilEventType.DISENCHANTMENT);
        assertEquals(expected, cost);
    }
}
