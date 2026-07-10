package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.Stats;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class StatsCommandTest extends DisenchantmentTestBase {

    @Test
    void givenLoggingDisabled_stats_sendsDisabledMessage() {
        PlayerMock player = server.addPlayer("Admin");
        player.addAttachment(plugin, "disenchantment.command.stats", true);

        Stats.execute(player, new String[]{"stats"});

        String msg = player.nextMessage();
        assertNotNull(msg, "Should receive a message");
        assertTrue(msg.contains("disabled") || msg.contains("not enabled"),
                "Message should mention stats are disabled, got: " + msg);
    }

    @Test
    void statsCommandIsRegisteredWithCorrectName() {
        assertEquals("stats", Stats.command.name());
    }
}
