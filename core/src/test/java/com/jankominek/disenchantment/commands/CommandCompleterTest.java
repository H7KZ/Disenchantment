package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.command.Command;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandCompleterTest extends DisenchantmentTestBase {

    private List<String> complete(PlayerMock player, String... args) {
        return new CommandCompleter().onTabComplete(player, Mockito.mock(Command.class), "disenchantment", args);
    }

    @Test
    void givenNoPermissions_whenTabCompleteSubcommand_thenNothingSuggested() {
        PlayerMock player = server.addPlayer("Nobody");

        assertTrue(complete(player, "").isEmpty());
        assertTrue(complete(player, "re").isEmpty());
    }

    @Test
    void givenOnlyHelpPermission_whenTabCompleteSubcommand_thenOnlyHelpSuggested() {
        PlayerMock player = server.addPlayer("Helper");
        player.addAttachment(plugin, "disenchantment.command.help", true);

        assertEquals(List.of("help"), complete(player, ""));
        assertTrue(complete(player, "rel").isEmpty(), "reload requires its own permission");
    }

    @Test
    void givenAllCommandPermission_whenTabCompleteSubcommand_thenEverythingSuggested() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(plugin, "disenchantment.command.all", true);

        List<String> suggestions = complete(admin, "");

        assertEquals(CommandRegister.commands.size(), suggestions.size());
        assertTrue(suggestions.contains("reload"));
    }

    @Test
    void givenNoPermissionForSubcommand_whenTabCompleteItsArguments_thenNothingSuggested() {
        PlayerMock player = server.addPlayer("Nobody");

        assertTrue(complete(player, "maintenance", "").isEmpty());
    }

    @Test
    void givenPermissionForSubcommand_whenTabCompleteItsArguments_thenSuggested() {
        PlayerMock player = server.addPlayer("Maint");
        player.addAttachment(plugin, "disenchantment.command.maintenance", true);

        assertEquals(java.util.Set.of("on", "off"), new java.util.HashSet<>(complete(player, "maintenance", "")));
    }

    @Test
    void givenNoPermission_whenExecuteCommand_thenExactlyOneDenialMessage() {
        PlayerMock player = server.addPlayer("Nobody");

        CommandRegister.commands.get("reload").execute(player, new String[]{"reload"});

        assertNotNull(player.nextMessage());
        assertNull(player.nextMessage(), "Only one permission denial message is expected");
    }
}
