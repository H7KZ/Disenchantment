package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigChancesAndCostsTest extends DisenchantmentTestBase {

    // ----------------------------------------------------------------------------------------------------
    // Feature 1: enchantment-chances

    @Test
    void givenNoChanceConfigured_whenGetEnchantmentChance_thenDefaultsToOne() {
        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("mending"));
        assertEquals(1.0, Config.Shatterment.getEnchantmentChance("mending"));
    }

    @Test
    void givenChanceConfigured_whenGetEnchantmentChance_thenReturnsConfiguredValue() {
        setConfig("disenchantment.enchantment-chances.mending", 0.5);
        assertEquals(0.5, Config.Disenchantment.getEnchantmentChance("mending"));
        // namespaced key form should also resolve to the short-key config entry
        assertEquals(0.5, Config.Disenchantment.getEnchantmentChance("minecraft:mending"));
    }

    @Test
    void givenSetEnchantmentChance_whenGetEnchantmentChance_thenPersistsAndClamps() {
        Config.Disenchantment.setEnchantmentChance("sharpness", 1.5); // out of range, clamps to 1.0
        assertEquals(1.0, Config.Disenchantment.getEnchantmentChance("sharpness"));

        Config.Disenchantment.setEnchantmentChance("sharpness", -0.5); // clamps to 0.0
        assertEquals(0.0, Config.Disenchantment.getEnchantmentChance("sharpness"));
    }

    @Test
    void givenShattermentChanceConfigured_whenGetEnchantmentChances_thenReturnsMap() {
        setConfig("shatterment.enchantment-chances.sharpness", 0.8);
        assertEquals(0.8, Config.Shatterment.getEnchantmentChances().get("sharpness"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Feature 2: mixed int/map enchantment-costs format (backward compat)

    @Test
    void givenLegacyIntegerCost_whenGetEnchantmentCosts_thenParsedAsXpOnly() {
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness", 5);

        assertEquals(5, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("sharpness"));
        assertTrue(Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts().isEmpty());
    }

    @Test
    void givenMapShapedCost_whenGetEnchantmentCosts_thenParsesXpAndEconomySeparately() {
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.xp", 10);
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.economy", 500.0);

        assertEquals(10, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("mending"));
        assertEquals(500.0, Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts().get("mending"));
    }

    @Test
    void givenMixedLegacyAndMapShapedCosts_whenGetEnchantmentCosts_thenBothParsedCorrectly() {
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.xp", 10);
        setConfig("disenchantment.anvil.repair.enchantment-costs.mending.economy", 500.0);
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness", 5);

        assertEquals(10, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("mending"));
        assertEquals(5, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("sharpness"));
        assertEquals(500.0, Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts().get("mending"));
        assertFalse(Config.Disenchantment.Anvil.Repair.getEnchantmentEconomyCosts().containsKey("sharpness"));
    }

    // ----------------------------------------------------------------------------------------------------
    // Feature 3: max-cost

    @Test
    void givenDefaultConfig_whenGetMaxCost_thenIsUncapped() {
        assertEquals(-1, Config.Disenchantment.Anvil.Repair.getMaxCost());
        assertEquals(-1, Config.Shatterment.Anvil.Repair.getMaxCost());
    }

    @Test
    void givenSetMaxCost_whenGetMaxCost_thenPersists() {
        Config.Disenchantment.Anvil.Repair.setMaxCost(20);
        assertEquals(20, Config.Disenchantment.Anvil.Repair.getMaxCost());
    }
}
