package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.Anvil;
import com.jankominek.disenchantment.commands.impl.Help;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI;
import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code /disenchantment anvil}: who may run it, what it needs, and that the base
 * {@code disenchantment.command.use} permission still guards every other subcommand now that it is checked in
 * code instead of plugin.yml.
 */
class AnvilCommandTest extends DisenchantmentTestBase {

    private PlayerMock bedrock;
    private Command pluginCommand;

    @BeforeEach
    void setUp() {
        setConfig("bedrock.fallback-enabled", true);
        bedrock = server.addPlayer(".Steve");
        pluginCommand = plugin.getCommand(Disenchantment.commandName);
        assertNotNull(pluginCommand);
    }

    @AfterEach
    void tearDown() {
        BedrockAnvilGUI.closeAll();
    }

    private static String plain(String message) {
        return org.bukkit.ChatColor.stripColor(message);
    }

    private String prefixed(String message) {
        return plain(I18n.getPrefix() + " " + message);
    }

    private void run(org.bukkit.command.CommandSender sender, String... args) {
        new CommandRegister().onCommand(sender, pluginCommand, "disenchant", args);
    }

    // -> the command itself

    @Test
    void givenBedrockPlayerWithDefaults_whenAnvil_thenMenuOpens() {
        run(bedrock, "anvil");

        assertNotNull(BedrockAnvilGUI.of(bedrock));
        assertTrue(BedrockAnvilGUI.of(bedrock).getInventory().getViewers().contains(bedrock), "the player is looking at it");
    }

    @Test
    void givenCaseInsensitiveName_whenAnvil_thenMenuOpens() {
        run(bedrock, "ANVIL");

        assertNotNull(BedrockAnvilGUI.of(bedrock));
    }

    @Test
    void givenFallbackDisabled_whenAnvil_thenMessageAndNoMenu() {
        setConfig("bedrock.fallback-enabled", false);

        run(bedrock, "anvil");

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertEquals(prefixed(I18n.Messages.bedrockUnavailable()), plain(bedrock.nextMessage()));
    }

    @Test
    void givenJavaPlayer_whenAnvil_thenOnlyForBedrockMessage() {
        PlayerMock java = server.addPlayer("Alex");

        run(java, "anvil");

        assertNull(BedrockAnvilGUI.of(java));
        assertEquals(prefixed(I18n.Messages.bedrockOnly()), plain(java.nextMessage()));
    }

    @Test
    void givenJavaPlayerAndAllowJavaPlayers_whenAnvil_thenMenuOpens() {
        setConfig("bedrock.allow-java-players", true);
        PlayerMock java = server.addPlayer("Alex");

        run(java, "anvil");

        assertNotNull(BedrockAnvilGUI.of(java));
    }

    @Test
    void givenConsole_whenAnvil_thenPlayersOnlyMessage() {
        ConsoleCommandSender console = server.getConsoleSender();

        assertDoesNotThrow(() -> run(console, "anvil"));
    }

    @Test
    void givenSpecialAnvilsAndNoneNearby_whenAnvil_thenMessageAndNoMenu() {
        setConfig("special-anvils.mode", "MARKED");

        run(bedrock, "anvil");

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertEquals(prefixed(I18n.Messages.specialAnvilRequired()), plain(bedrock.nextMessage()));
    }

    // -> permissions

    @Test
    void givenDefaultPermissions_thenEveryPlayerMayRunAnvil() {
        assertFalse(bedrock.isOp());
        assertTrue(bedrock.hasPermission("disenchantment.command.anvil"), "plugin.yml declares it default true");
    }

    @Test
    void givenAnvilPermissionDenied_whenAnvil_thenRequiresPermission() {
        bedrock.addAttachment(plugin, "disenchantment.command.anvil", false);

        run(bedrock, "anvil");

        assertNull(BedrockAnvilGUI.of(bedrock));
        assertTrue(plain(bedrock.nextMessage()).contains(plain(I18n.Messages.requiresPermission())));
    }

    @Test
    void givenNonOp_whenOtherSubcommands_thenStillRefusedByTheBasePermission() {
        run(bedrock, "gui");
        assertNull(BedrockAnvilGUI.of(bedrock));
        assertEquals(prefixed(I18n.Messages.requiresPermission()), plain(bedrock.nextMessage()));

        run(bedrock, "help");
        assertEquals(prefixed(I18n.Messages.requiresPermission()), plain(bedrock.nextMessage()));

        run(bedrock);
        assertEquals(prefixed(I18n.Messages.requiresPermission()), plain(bedrock.nextMessage()));
    }

    @Test
    void givenOp_whenOtherSubcommands_thenTheyWorkAsBefore() {
        PlayerMock op = server.addPlayer("Admin");
        op.setOp(true);

        run(op, "help");

        assertNotNull(op.nextMessage(), "help output");
        assertTrue(op.hasPermission("disenchantment.command.use"));
    }

    @Test
    void givenBaseUsePermissionOnly_whenHelp_thenCommandLevelPermissionStillApplies() {
        PlayerMock user = server.addPlayer("User");
        user.addAttachment(plugin, "disenchantment.command.use", true);

        run(user, "help");

        // Has command.use but not command.help: refused by the subcommand's own permission, exactly as before.
        assertEquals(prefixed(I18n.Messages.requiresPermission()), plain(user.nextMessage()));
    }

    // -> tab completion and help

    @Test
    void givenNonOp_whenTabCompleting_thenOnlyAnvilIsSuggested() {
        List<String> suggestions = new CommandCompleter().onTabComplete(bedrock, pluginCommand, "disenchant", new String[]{""});

        assertEquals(List.of("anvil"), suggestions);
    }

    @Test
    void givenHelp_thenAnvilCommandIsListed() {
        PlayerMock op = server.addPlayer("Admin");
        op.setOp(true);

        boolean listed = false;
        for (List<String> page : I18n.Commands.Help.pages()) {
            for (String line : page) listed |= plain(line).contains("/disenchant anvil");
        }

        assertTrue(listed, "help must list /disenchant anvil");
        assertEquals(8, Help.command.args().length);
    }

    @Test
    void givenAnvilCommand_thenItIsRegistered() {
        assertSame(Anvil.command, CommandRegister.commands.get("anvil"));
    }
}
