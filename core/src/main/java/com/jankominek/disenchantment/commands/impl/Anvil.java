package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.guis.impl.BedrockAnvilGUI;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "anvil" subcommand, which opens the Bedrock chest-menu anvil: a stand-in for the real anvil for
 * clients (Geyser/Bedrock) that cannot see custom anvil results. It works only while {@code bedrock.fallback-enabled}
 * is on, only for Bedrock players unless {@code bedrock.allow-java-players} is on, and, when
 * {@code special-anvils.mode} is not {@code ALL}, only near a special anvil.
 * <p>
 * Unlike the other subcommands its permission ({@code disenchantment.command.anvil}) defaults to {@code true},
 * and {@code CommandRegister} exempts it from the base {@code disenchantment.command.use} check.
 */
public class Anvil {
    /**
     * The command definition for the anvil subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "anvil",
            PermissionGroupType.COMMAND_ANVIL,
            new String[]{},
            false,
            Anvil::execute,
            Anvil::complete
    );

    /**
     * Opens the menu for the sender if every condition for using it is met; otherwise tells the player why not.
     *
     * @param s    the command sender (must be a player)
     * @param args the command arguments (unused)
     */
    public static void execute(CommandSender s, String[] args) {
        if (!(s instanceof Player player)) {
            s.sendMessage(I18n.getPrefix() + " " + I18n.Messages.bedrockPlayersOnly());
            return;
        }

        BedrockAnvilGUI.tryOpen(player, null);
    }

    /**
     * Returns an empty list since the anvil command has no arguments to complete.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return an empty list
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
