package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.enabled;

/**
 * Handles the "status" subcommand which displays the current enabled/disabled
 * state of the plugin globally, and for the disenchantment and shatterment
 * features individually.
 */
public class Status {
    /**
     * The command definition for the status subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "status",
            PermissionGroupType.COMMAND_STATUS,
            new String[]{},
            false,
            Status::execute,
            Status::complete
    );

    /**
     * Displays the plugin status including global, disenchantment, and shatterment states.
     *
     * @param s           the command sender
     * @param ignoredArgs the command arguments (unused)
     */
    public static void execute(CommandSender s, String[] ignoredArgs) {
        s.sendMessage(I18n.Commands.Status.title());
        s.sendMessage(I18n.Commands.Status.global(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.disenchantment(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.shatterment(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
    }

    /**
     * Returns an empty list since the status command has no arguments to complete.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return an empty list
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
