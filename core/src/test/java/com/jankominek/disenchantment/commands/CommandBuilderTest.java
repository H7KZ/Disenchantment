package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest extends DisenchantmentTestBase {

    private CommandBuilder builder(String[] validArgs, boolean requireArgs, AtomicBoolean executed) {
        return new CommandBuilder(
                "test",
                PermissionGroupType.COMMAND_HELP,
                validArgs,
                requireArgs,
                (sender, args) -> executed.set(true),
                (sender, args) -> java.util.List.of()
        );
    }

    // -> argument validation

    @Test
    void givenValidArg_whenExecute_thenCommandRuns() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.command.help", true);
        AtomicBoolean ran = new AtomicBoolean(false);
        CommandBuilder cmd = builder(new String[]{"on", "off"}, true, ran);

        cmd.execute(player, new String[]{"test", "on"});

        assertTrue(ran.get(), "Command must execute for a valid argument");
    }

    @Test
    void givenInvalidArg_whenExecute_thenCommandDoesNotRun() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.command.help", true);
        AtomicBoolean ran = new AtomicBoolean(false);
        CommandBuilder cmd = builder(new String[]{"on", "off"}, true, ran);

        cmd.execute(player, new String[]{"test", "maybe"});

        assertFalse(ran.get(), "Command must not execute for an invalid argument");
    }

    @Test
    void givenRequireArgsFalse_whenExecuteWithAnyArg_thenCommandRuns() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.command.help", true);
        AtomicBoolean ran = new AtomicBoolean(false);
        CommandBuilder cmd = builder(new String[]{"on", "off"}, false, ran);

        cmd.execute(player, new String[]{"test", "anything"});

        assertTrue(ran.get(), "Command must execute regardless of arg when requireArgs=false");
    }

    @Test
    void givenArgCaseInsensitive_whenExecute_thenCommandRuns() {
        PlayerMock player = server.addPlayer("TestPlayer");
        player.addAttachment(plugin, "disenchantment.command.help", true);
        AtomicBoolean ran = new AtomicBoolean(false);
        CommandBuilder cmd = builder(new String[]{"on", "off"}, true, ran);

        cmd.execute(player, new String[]{"test", "ON"});

        assertTrue(ran.get(), "Arg matching must be case-insensitive");
    }
}
