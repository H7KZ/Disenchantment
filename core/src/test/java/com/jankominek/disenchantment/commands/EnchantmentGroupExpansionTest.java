package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.DisenchantEnchantments;
import com.jankominek.disenchantment.commands.impl.ShatterEnchantments;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantmentGroupExpansionTest extends DisenchantmentTestBase {

    @Test
    void givenGroupName_whenDisenchantEnchantmentsExecuted_thenAllMembersUpdated() {
        setConfig("enchantment-groups.combat", List.of("sharpness", "smite"));

        PlayerMock player = server.addPlayer("TestPlayer");

        DisenchantEnchantments.execute(player, new String[]{"disenchant:enchantments", "combat", "disable"});

        Map<String, EnchantmentStateType> states = Config.Disenchantment.getEnchantmentStates();

        assertEquals(EnchantmentStateType.DISABLE, states.get("sharpness"));
        assertEquals(EnchantmentStateType.DISABLE, states.get("smite"));
    }

    @Test
    void givenGroupName_whenShatterEnchantmentsExecuted_thenAllMembersUpdated() {
        setConfig("enchantment-groups.utility", List.of("efficiency", "unbreaking"));

        PlayerMock player = server.addPlayer("TestPlayer");

        ShatterEnchantments.execute(player, new String[]{"shatter:enchantments", "utility", "keep"});

        Map<String, EnchantmentStateType> states = Config.Shatterment.getEnchantmentStates();

        assertEquals(EnchantmentStateType.KEEP, states.get("efficiency"));
        assertEquals(EnchantmentStateType.KEEP, states.get("unbreaking"));
    }

    @Test
    void givenGroupNameInTabComplete_whenComplete_thenGroupNameSuggested() {
        setConfig("enchantment-groups.combat", List.of("sharpness"));

        List<String> suggestions = DisenchantEnchantments.complete(server.addPlayer("TestPlayer"), new String[]{"disenchant:enchantments", "com"});

        assertEquals(true, suggestions.contains("combat"));
    }
}
