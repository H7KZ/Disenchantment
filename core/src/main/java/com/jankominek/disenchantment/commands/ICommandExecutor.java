package com.jankominek.disenchantment.commands;

import org.bukkit.command.CommandSender;

/**
 * Functional interface for executing a subcommand's logic.
 */
@FunctionalInterface
public interface ICommandExecutor {
    /**
     * Executes the command logic.
     *
     * @param s the command sender
     * @param a the command arguments
     */
    void execute(CommandSender s, String[] a);
}
