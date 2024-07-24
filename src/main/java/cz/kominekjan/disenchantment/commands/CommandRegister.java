package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.commands.impl.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandRegister implements CommandExecutor {
    public final static Map<String, Command> commands;

    static {
        commands = new HashMap<>();
        commands.put("help", Help.command);
        commands.put("status", Status.command);
        commands.put("toggle", Toggle.command);
        commands.put("repair", Repair.command);
        commands.put("sound", Sound.command);
        commands.put("materials", Materials.command);
        commands.put("enchantments", Enchantments.command);
        commands.put("gui", GUI.command);
    }

    private boolean executeCommand(String n, CommandSender s, String[] a) {
        return getCommand(n).execute(s, a);
    }

    private Command getCommand(String n) {
        return commands.get(n.toLowerCase()) == null ? commands.get("help") : commands.get(n.toLowerCase());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, String[] args) {
        if (args.length == 0) return executeCommand("help", sender, args);

        return executeCommand(args[0].toLowerCase(), sender, args);
    }
}
