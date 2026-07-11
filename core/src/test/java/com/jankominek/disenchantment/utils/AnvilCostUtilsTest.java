package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.types.AnvilEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

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

    @Test
    void enchantmentCostOverride_usesFixedCost() {
        Map<String, Integer> overrides = Map.of("minecraft:mending", 30);
        int cost = AnvilCostUtils.costForEnchantment("minecraft:mending", 1, 10.0, 0.25, overrides);
        assertEquals(30, cost);
    }

    @Test
    void enchantmentCostOverride_fallsBackToFormula_whenNoOverride() {
        Map<String, Integer> overrides = Map.of();
        // base=10, level=2, multiply=1.0 → formula: 10 + 2*1.0 = 12
        int cost = AnvilCostUtils.costForEnchantment("minecraft:protection", 2, 10.0, 1.0, overrides);
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

    // ----------------------------------------------------------------------------------------------------
    // max-cost cap (Feature 3)

    @Test
    void givenMaxCostBelowCalculatedCost_whenCountCost_thenCapsResult() {
        // base=10, multiplier=0.25, level=5 → 11.25 → 11, capped to 5
        setConfig("disenchantment.anvil.repair.max-cost", 5);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(5, cost);
    }

    @Test
    void givenMaxCostOfMinusOne_whenCountCost_thenNoCapApplied() {
        setConfig("disenchantment.anvil.repair.max-cost", -1);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(11, cost);
    }

    @Test
    void givenMaxCostAboveCalculatedCost_whenCountCost_thenUnaffected() {
        setConfig("disenchantment.anvil.repair.max-cost", 1000);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.DISENCHANTMENT);
        assertEquals(11, cost);
    }

    @Test
    void givenShattermentMaxCost_whenCountCost_thenCapsResult() {
        setConfig("shatterment.anvil.repair.max-cost", 3);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        int cost = AnvilCostUtils.countAnvilCost(List.of(sharpnessV), AnvilEventType.SHATTERMENT);
        assertEquals(3, cost);
    }

    // ----------------------------------------------------------------------------------------------------
    // Per-enchantment economy overrides (Feature 2)

    @Test
    void givenNoEconomyOverrides_whenEconomyCostForEnchantments_thenReturnsFlatCost() {
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        double cost = AnvilCostUtils.economyCostForEnchantments(List.of(sharpnessV), 100.0, Map.of());
        assertEquals(100.0, cost);
    }

    @Test
    void givenOverrideForAllEnchantments_whenEconomyCostForEnchantments_thenSumsOverridesOnly() {
        IPluginEnchantment mendingI = EnchantmentUtils.remapEnchantment(enchantment("mending"), 1);
        Map<String, Double> overrides = Map.of("mending", 500.0);
        double cost = AnvilCostUtils.economyCostForEnchantments(List.of(mendingI), 100.0, overrides);
        assertEquals(500.0, cost);
    }

    @Test
    void givenMixOfOverriddenAndPlainEnchantments_whenEconomyCostForEnchantments_thenSumsOverridesPlusFlatOnce() {
        IPluginEnchantment mendingI = EnchantmentUtils.remapEnchantment(enchantment("mending"), 1);
        IPluginEnchantment sharpnessV = EnchantmentUtils.remapEnchantment(enchantment("sharpness"), 5);
        Map<String, Double> overrides = Map.of("mending", 500.0);
        double cost = AnvilCostUtils.economyCostForEnchantments(List.of(mendingI, sharpnessV), 100.0, overrides);
        assertEquals(600.0, cost); // 500 (mending override) + 100 (flat, for sharpness which has no override)
    }
}
