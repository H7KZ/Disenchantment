package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Repair {
    public static final CommandUnit unit = new CommandUnit("repair", "disenchantment.repair", "You don't have permission to use this command.", new String[]{"enable", "disable", "base", "multiply"}, false, Repair::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Repair cost configuration", ChatColor.GRAY);
            s.sendMessage(ChatColor.GRAY + "|");
            s.sendMessage(ChatColor.GRAY + "| Repair cost is " + (config.getBoolean("enable-repair-cost") ? ChatColor.GREEN : ChatColor.RED) + (config.getBoolean("enable-repair-cost") ? "Enabled" : "Disabled"));
            s.sendMessage(ChatColor.GRAY + "| Base value: " + config.getInt("base"));
            s.sendMessage(ChatColor.GRAY + "| Multiply value: " + config.getDouble("multiply"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                config.set("enable-repair-cost", true);
                plugin.saveConfig();
                sendMessage(s, "Repair cost enabled", ChatColor.GREEN);
                break;
            }
            case "disable": {
                config.set("enable-repair-cost", false);
                plugin.saveConfig();
                sendMessage(s, "Repair cost disabled", ChatColor.GREEN);
                break;
            }
            case "reset": {
                if (args.length == 2) {
                    sendMessage(s, "You must specify a value");
                    break;
                }

                if (args[2].equalsIgnoreCase("enable")) {
                    config.set("enable-repair-reset", true);
                    plugin.saveConfig();
                    sendMessage(s, "Repair cost reset enabled", ChatColor.GREEN);
                } else if (args[2].equalsIgnoreCase("disable")) {
                    config.set("enable-repair-reset", false);
                    plugin.saveConfig();
                    sendMessage(s, "Repair cost reset disabled", ChatColor.GREEN);
                } else {
                    sendMessage(s, "Unknown argument!");
                }

                break;
            }
            case "base": {
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
            case "multiply": {
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
            default: {
                sendMessage(s, "Unknown argument!");
            }
        }
    }
}
