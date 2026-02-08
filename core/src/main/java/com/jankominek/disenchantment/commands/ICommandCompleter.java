package com.jankominek.disenchantment.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Functional interface for providing tab completion suggestions for a subcommand.
 */
@FunctionalInterface
public interface ICommandCompleter {
    /**
     * Returns tab completion suggestions for the given arguments.
     *
     * @param s the command sender requesting completions
     * @param a the current command arguments
     * @return a list of completion suggestions
     */
    List<String> onTabComplete(CommandSender s, String[] a);
}
