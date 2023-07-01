package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.CommandSender;

@FunctionalInterface
interface CommandExecutor {
    boolean execute(CommandSender s, String[] args);
}