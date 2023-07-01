package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Config {
    public static final CommandUnit unit = new CommandUnit("config", "disenchantment.config", "You don't have permission to use this command.", new String[]{"base", "multiply"}, false, Config::command);

    public static boolean command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Base value: " + config.getInt("base"), ChatColor.DARK_GREEN);
            sendMessage(s, "Multiply value: " + config.getDouble("multiply"), ChatColor.DARK_GREEN);
            return true;
        }

        switch (args[1]) {
            case "base" -> {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("base", Integer.parseInt(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Base value set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid integer");
                }
            }
            case "multiply" -> {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                try {
                    config.set("multiply", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    sendMessage(s, "Multiply value set to " + args[2], ChatColor.GREEN);
                } catch (NumberFormatException e) {
                    sendMessage(s, "You must specify a valid double");
                }
            }
            default -> sendMessage(s, "Unknown config option");
        }

        return true;
    }
}
