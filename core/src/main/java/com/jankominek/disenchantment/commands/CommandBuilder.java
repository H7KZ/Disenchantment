package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents a plugin subcommand with its name, permission, arguments, executor, and tab completer.
 * Handles permission checks and argument validation before delegating execution.
 *
 * @param name        The name of the subcommand.
 * @param permission  The permission group required to execute this command.
 * @param args        The valid argument values for this command.
 * @param requireArgs Whether the command requires arguments to be validated against {@link #args()}.
 * @param executor    The executor that performs the command logic.
 * @param completer   The tab completer that provides suggestions for this command.
 */
public record CommandBuilder(String name, PermissionGroupType permission, String[] args, boolean requireArgs,
                             ICommandExecutor executor, ICommandCompleter completer) {
    /**
     * Constructs a new CommandBuilder.
     *
     * @param name        the subcommand name
     * @param permission  the permission group required to use this command
     * @param args        the valid argument values
     * @param requireArgs whether arguments must match the valid argument list
     * @param executor    the command executor
     * @param completer   the tab completer
     */
    public CommandBuilder {
    }

    /**
     * Executes this command after checking permissions and validating arguments.
     * Sends an error message to the sender if permission is denied or arguments are invalid.
     *
     * @param sender the command sender
     * @param args   the command arguments
     * @return true after execution or error handling
     */
    public boolean execute(CommandSender sender, String[] args) {
        if (!permission.hasPermission(sender)) {
            sender.sendMessage(I18n.getPrefix() + " " + I18n.Messages.requiresPermission());
            return true;
        }

        if (this.args.length == 0 || !requireArgs) {
            executor.execute(sender, args);
            return true;
        }

        boolean areArgsValid = true;

        for (String arg : this.args)
            if (arg.equalsIgnoreCase(args[1])) {
                areArgsValid = false;
                break;
            }

        if (areArgsValid) {
            sender.sendMessage(I18n.getPrefix() + " " + I18n.Messages.invalidArgument());
            return true;
        }

        executor.execute(sender, args);

        return true;
    }

    /**
     * Delegates tab completion to the configured completer.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of tab completion suggestions
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return completer.onTabComplete(sender, args);
    }
}
