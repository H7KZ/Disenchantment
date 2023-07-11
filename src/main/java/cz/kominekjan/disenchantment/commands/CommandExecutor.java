package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.CommandSender;

@FunctionalInterface
interface CommandExecutor {
    void execute(CommandSender s, String[] args);
}