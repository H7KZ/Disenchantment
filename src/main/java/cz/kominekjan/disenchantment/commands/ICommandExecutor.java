package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface ICommandExecutor {
    void execute(CommandSender s, String[] args);
}
