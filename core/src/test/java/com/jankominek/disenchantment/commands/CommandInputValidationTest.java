package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.*;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.utils.InputUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Numeric command arguments must reject NaN, Infinity and (where meaningless) negative values
 * instead of writing them to config.yml.
 */
class CommandInputValidationTest extends DisenchantmentTestBase {

    // -> InputUtils

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-Infinity", "abc", "", "  "})
    void givenNonFiniteOrGarbage_whenParseFinite_thenNull(String raw) {
        assertNull(InputUtils.parseFinite(raw));
    }

    @Test
    void givenFiniteNumbers_whenParseFinite_thenParsed() {
        assertEquals(2.5, InputUtils.parseFinite("2.5"));
        assertEquals(-3.0, InputUtils.parseFinite("-3"));
        assertEquals(0.0, InputUtils.parseFinite("0"));
    }

    // -> repair

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-1", "-0.5", "abc"})
    void givenBadValue_whenDisenchantRepairBase_thenConfigUnchanged(String raw) {
        PlayerMock admin = server.addPlayer("Admin");
        double before = Config.Disenchantment.Anvil.Repair.getBaseCost();

        DisenchantRepair.execute(admin, new String[]{"disenchant:repair", "base", raw});

        assertEquals(before, Config.Disenchantment.Anvil.Repair.getBaseCost());
        assertNotNull(admin.nextMessage(), "Sender must receive an error");
    }

    @Test
    void givenGoodValue_whenDisenchantRepairBase_thenSet() {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantRepair.execute(admin, new String[]{"disenchant:repair", "base", "12.5"});

        assertEquals(12.5, Config.Disenchantment.Anvil.Repair.getBaseCost());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-2"})
    void givenBadValue_whenShatterRepairMultiply_thenConfigUnchanged(String raw) {
        PlayerMock admin = server.addPlayer("Admin");
        double before = Config.Shatterment.Anvil.Repair.getCostMultiplier();

        ShatterRepair.execute(admin, new String[]{"shatter:repair", "multiply", raw});

        assertEquals(before, Config.Shatterment.Anvil.Repair.getCostMultiplier());
    }

    // -> economy

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-100"})
    void givenBadValue_whenEconomyCost_thenConfigUnchanged(String raw) {
        PlayerMock admin = server.addPlayer("Admin");
        double dBefore = Config.Disenchantment.Economy.getCost();
        double sBefore = Config.Shatterment.Economy.getCost();

        DisenchantEconomy.execute(admin, new String[]{"disenchant:economy", "cost", raw});
        ShatterEconomy.execute(admin, new String[]{"shatter:economy", "cost", raw});

        assertEquals(dBefore, Config.Disenchantment.Economy.getCost());
        assertEquals(sBefore, Config.Shatterment.Economy.getCost());
    }

    @Test
    void givenGoodValue_whenEconomyCost_thenSet() {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantEconomy.execute(admin, new String[]{"disenchant:economy", "cost", "250"});

        assertEquals(250.0, Config.Disenchantment.Economy.getCost());
    }

    // -> sound

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-1"})
    void givenBadValue_whenSoundVolumeAndPitch_thenConfigUnchanged(String raw) {
        PlayerMock admin = server.addPlayer("Admin");
        double volume = Config.Disenchantment.Anvil.Sound.getVolume();
        double pitch = Config.Shatterment.Anvil.Sound.getPitch();

        DisenchantSound.execute(admin, new String[]{"disenchant:sound", "volume", raw});
        ShatterSound.execute(admin, new String[]{"shatter:sound", "pitch", raw});

        assertEquals(volume, Config.Disenchantment.Anvil.Sound.getVolume());
        assertEquals(pitch, Config.Shatterment.Anvil.Sound.getPitch());
    }

    // -> chances

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-0.1", "1.5"})
    void givenBadChance_whenChancesCommand_thenNotStored(String raw) {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantChances.execute(admin, new String[]{"disenchant:chances", "sharpness", raw});
        ShatterChances.execute(admin, new String[]{"shatter:chances", "sharpness", raw});

        assertFalse(Config.Disenchantment.getEnchantmentChances().containsKey("sharpness"));
        assertFalse(Config.Shatterment.getEnchantmentChances().containsKey("sharpness"));
    }

    @Test
    void givenGoodChance_whenChancesCommand_thenStored() {
        PlayerMock admin = server.addPlayer("Admin");

        DisenchantChances.execute(admin, new String[]{"disenchant:chances", "sharpness", "0.5"});

        assertEquals(0.5, Config.Disenchantment.getEnchantmentChances().get("sharpness"));
    }
}
