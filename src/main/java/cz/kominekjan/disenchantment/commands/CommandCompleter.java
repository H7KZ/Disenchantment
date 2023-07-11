package cz.kominekjan.disenchantment.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import static cz.kominekjan.disenchantment.commands.CommandRegister.commands;

public class CommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String c : commands.keySet()) {
                if (c.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(c);
                }
            }

            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            for (String arg : commands.get("config").args) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(arg);
                }
            }

            return result;
        }

        return null;
    }
}
