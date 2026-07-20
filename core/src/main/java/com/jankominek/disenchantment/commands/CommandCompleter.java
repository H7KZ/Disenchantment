package com.jankominek.disenchantment.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.commands.CommandRegister.commands;

/**
 * Provides tab completion for the plugin's main command by delegating to
 * registered subcommand completers. At the first argument level, it suggests
 * matching subcommand names; at deeper levels, it delegates to the
 * corresponding subcommand's completer.
 */
public class CommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String c : commands.keySet()) {
                if (c.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(c);
                }
            }

            return result;
        }

        for (CommandBuilder cmd : commands.values()) {
            if (cmd.name().equalsIgnoreCase(args[0])) result = cmd.onTabComplete(sender, args);
        }

        return result;
    }
}
