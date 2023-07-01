package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CommandRegister implements CommandExecutor {
    public final static Map<String, CommandUnit> commands = Map.ofEntries(
            Map.entry("help", Help.unit),
            Map.entry("status", Status.unit),
            Map.entry("toggle", Toggle.unit),
            Map.entry("config", Config.unit)
    );

    private boolean executeCommand(String n, CommandSender s, String[] a) {
        return getCommand(n).execute(s, a);
    }

    private CommandUnit getCommand(String n) {
        return commands.get(n.toLowerCase()) == null ? commands.get("help") : commands.get(n.toLowerCase());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return executeCommand("help", sender, args);

        return executeCommand(args[0], sender, args);
    }
}
