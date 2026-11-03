package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.utils.ConfigUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The per-anvil-event config reads (chances, XP costs, economy costs) are cached; the caches must be
 * dropped on setters, on {@link Config#invalidateCaches()} and on a config reload.
 */
class ConfigCacheTest extends DisenchantmentTestBase {

    // -> reads are cached

    @Test
    void givenRepeatedReads_whenNoConfigChange_thenSameInstanceReturned() {
        setConfig("disenchantment.enchantment-chances.mending", 0.5);
        setConfig("shatterment.anvil.repair.enchantment-costs.sharpness", 3);
        setConfig("shatterment.anvil.repair.enchantment-costs.mending.economy", 9.0);

        assertSame(Config.Disenchantment.getEnchantmentChances(), Config.Disenchantment.getEnchantmentChances());
        assertSame(Config.Shatterment.Anvil.Repair.getEnchantmentCosts(), Config.Shatterment.Anvil.Repair.getEnchantmentCosts());
        assertSame(Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts(), Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts());
        assertSame(Config.Disenchantment.Anvil.Repair.getEnchantmentCosts(), Config.Disenchantment.Anvil.Repair.getEnchantmentCosts());
        assertSame(Config.Shatterment.getEnchantmentChances(), Config.Shatterment.getEnchantmentChances());
    }

    @Test
    void givenCachedMaps_whenCallerMutates_thenRejected() {
        setConfig("disenchantment.enchantment-chances.mending", 0.5);

        assertThrows(UnsupportedOperationException.class, () -> Config.Disenchantment.getEnchantmentChances().put("x", 1.0));
    }

    // -> invalidation

    @Test
    void givenCachedChance_whenSetter_thenNewValueVisible() {
        assertTrue(Config.Disenchantment.getEnchantmentChances().isEmpty());
        assertTrue(Config.Shatterment.getEnchantmentChances().isEmpty());

        Config.Disenchantment.setEnchantmentChance("Mending", 0.25);
        Config.Shatterment.setEnchantmentChance("sharpness", 0.75);

        assertEquals(0.25, Config.Disenchantment.getEnchantmentChances().get("mending"));
        assertEquals(0.25, Config.Disenchantment.getEnchantmentChance("minecraft:mending"));
        assertEquals(0.75, Config.Shatterment.getEnchantmentChances().get("sharpness"));
    }

    @Test
    void givenCachedCosts_whenInvalidateCaches_thenRereadFromConfig() {
        setConfig("disenchantment.anvil.repair.enchantment-costs.sharpness", 5);
        setConfig("shatterment.anvil.repair.enchantment-costs.mending.xp", 7);
        setConfig("shatterment.anvil.repair.enchantment-costs.mending.economy", 70.0);
        assertEquals(5, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("sharpness"));
        assertEquals(70.0, Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts().get("mending"));

        // Direct config edits bypass the setters, so only an invalidation may surface them.
        Disenchantment.config.set("disenchantment.anvil.repair.enchantment-costs.sharpness", 6);
        Disenchantment.config.set("shatterment.anvil.repair.enchantment-costs.mending.economy", 80.0);
        assertEquals(5, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("sharpness"), "still cached");

        Config.invalidateCaches();

        assertEquals(6, Config.Disenchantment.Anvil.Repair.getEnchantmentCosts().get("sharpness"));
        assertEquals(80.0, Config.Shatterment.Anvil.Repair.getEnchantmentEconomyCosts().get("mending"));
    }

    @Test
    void givenCachedValues_whenConfigReloaded_thenCachesCleared() {
        Config.Disenchantment.setEnchantmentChance("mending", 0.1);
        assertEquals(0.1, Config.Disenchantment.getEnchantmentChances().get("mending"));
        // Simulate the admin editing config.yml by hand, then /disenchantment reload.
        Disenchantment.config.set("disenchantment.enchantment-chances.mending", 0.9);

        ConfigUtils.setupConfig();

        // The reload re-read the file (which holds the value persisted by the setter, 0.1).
        assertEquals(0.1, Config.Disenchantment.getEnchantmentChances().get("mending"));
        plugin.getConfig().set("disenchantment.enchantment-chances.mending", 0.4);
        Config.invalidateCaches();
        assertEquals(0.4, Config.Disenchantment.getEnchantmentChances().get("mending"));
    }
}
