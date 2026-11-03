package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.SpecialAnvilMode;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the typed {@code special-anvils} accessors and their fallbacks.
 */
class SpecialAnvilsConfigTest extends DisenchantmentTestBase {

    @Test
    void givenBundledDefaults_whenRead_thenBehaviourPreserving() {
        assertEquals(SpecialAnvilMode.ALL, Config.SpecialAnvils.getMode());
        assertEquals(Material.LAPIS_BLOCK, Config.SpecialAnvils.getBlockBelow());
        assertFalse(Config.SpecialAnvils.isAllowVirtualAnvils());
        assertTrue(Config.SpecialAnvils.isDropSpecialItem());
        assertFalse(Config.SpecialAnvils.isHintMessageEnabled());
        assertFalse(Config.SpecialAnvils.isRecipeEnabled());
        assertEquals(List.of("EEE", "EAE", "EEE"), Config.SpecialAnvils.getRecipeShape());
        assertEquals(Map.of('E', "EMERALD", 'A', "ANVIL"), Config.SpecialAnvils.getRecipeIngredients());
        assertTrue(Config.SpecialAnvils.validate().isEmpty());
    }

    @Test
    void givenModeValues_whenRead_thenParsedCaseInsensitively() {
        setConfig("special-anvils.mode", "marked");
        assertEquals(SpecialAnvilMode.MARKED, Config.SpecialAnvils.getMode());

        setConfig("special-anvils.mode", " Block_Below ");
        assertEquals(SpecialAnvilMode.BLOCK_BELOW, Config.SpecialAnvils.getMode());
    }

    @Test
    void givenUnknownMode_whenRead_thenFallsBackToAllAndWarns() {
        setConfig("special-anvils.mode", "MARKD");

        assertEquals(SpecialAnvilMode.ALL, Config.SpecialAnvils.getMode());
        assertEquals(1, Config.SpecialAnvils.validate().size());
    }

    @Test
    void givenBlockBelowValues_whenRead_thenResolvedOrDefaulted() {
        setConfig("special-anvils.block-below", "minecraft:emerald_block");
        assertEquals(Material.EMERALD_BLOCK, Config.SpecialAnvils.getBlockBelow());

        setConfig("special-anvils.block-below", "DIAMOND");
        assertEquals(Material.LAPIS_BLOCK, Config.SpecialAnvils.getBlockBelow(), "an item is not a block");
        assertEquals(1, Config.SpecialAnvils.validate().size());

        setConfig("special-anvils.block-below", "NOT_A_MATERIAL");
        assertEquals(Material.LAPIS_BLOCK, Config.SpecialAnvils.getBlockBelow());
        assertEquals(1, Config.SpecialAnvils.validate().size());
    }

    @Test
    void givenToggles_whenSet_thenReflected() {
        setConfig("special-anvils.allow-virtual-anvils", true);
        setConfig("special-anvils.drop-special-item", false);
        setConfig("special-anvils.hint-message", true);
        setConfig("special-anvils.recipe.enabled", true);

        assertTrue(Config.SpecialAnvils.isAllowVirtualAnvils());
        assertFalse(Config.SpecialAnvils.isDropSpecialItem());
        assertTrue(Config.SpecialAnvils.isHintMessageEnabled());
        assertTrue(Config.SpecialAnvils.isRecipeEnabled());
    }
}
