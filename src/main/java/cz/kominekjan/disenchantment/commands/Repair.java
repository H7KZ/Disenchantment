package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.utils.TextUtil.*;

public class Repair {
    public static final CommandUnit unit = new CommandUnit("repair", "disenchantment.repair", "You don't have permission to use this command.", new String[]{"enable", "disable", "reset", "base", "multiply"}, false, Repair::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(TextWithPrefix("Repair cost configuration"));
            s.sendMessage("");
            String builder = "";
            builder += ChatColor.GRAY + "Repair cost is ";
            builder += config.getBoolean("enable-repair-cost") ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Base value: " + config.getInt("base"));
            s.sendMessage(ChatColor.GRAY + "Multiply value: " + config.getDouble("multiply"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                config.set("enable-repair-cost", true);
                plugin.saveConfig();
                s.sendMessage(TextWithPrefixSuccess("Repair cost enabled"));
                break;
            }
            case "disable": {
                config.set("enable-repair-cost", false);
                plugin.saveConfig();
                s.sendMessage(TextWithPrefixSuccess("Repair cost disabled"));
                break;
            }
            case "reset": {
                if (args.length == 2) {
                    s.sendMessage(TextWithPrefixError("You must specify a value"));
                    break;
                }

                if (args[2].equalsIgnoreCase("enable")) {
                    config.set("enable-repair-reset", true);
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Repair cost reset enabled"));
                } else if (args[2].equalsIgnoreCase("disable")) {
                    config.set("enable-repair-reset", false);
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Repair cost reset disabled"));
                } else {
                    s.sendMessage(TextWithPrefixError("You must specify 'enable' or 'disable'"));
                }

                break;
            }
            case "base": {
                if (args.length == 2) {
                    s.sendMessage(TextWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    config.set("base", Integer.parseInt(args[2]));
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Base value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(TextWithPrefixError("You must specify a valid number"));
                }
            }
            case "multiply": {
                if (args.length == 2) {
                    s.sendMessage(TextWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    config.set("multiply", Double.parseDouble(args[2]));
                    plugin.saveConfig();
                    s.sendMessage(TextWithPrefixSuccess("Multiply value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(TextWithPrefixError("You must specify a valid number"));
                }
            }
            default: {
                s.sendMessage(TextWithPrefixError("Unknown argument"));
            }
        }
    }
}
