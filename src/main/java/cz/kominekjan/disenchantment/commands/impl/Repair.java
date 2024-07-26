package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.TextUtil.*;

public class Repair {
    public static final Command command = new Command(
            "repair",
            new String[]{"disenchantment.all", "disenchantment.command.repair"},
            "You don't have permission to use this command.",
            new String[]{"enable", "disable", "reset", "base", "multiply"},
            false,
            Repair::execute
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Repair cost configuration"));
            s.sendMessage("");
            String builder = "";
            builder += ChatColor.GRAY + "Repair cost is ";
            builder += getEnableRepairCost() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            builder += ChatColor.GRAY + "Repair reset is ";
            builder += getEnableRepairReset() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Base value: " + getBaseRepairCost());
            s.sendMessage(ChatColor.GRAY + "Multiply value: " + getRepairCostMultiplier());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                setEnableRepairCost(true);
                s.sendMessage(textWithPrefixSuccess("Repair cost enabled"));
                break;
            }
            case "disable": {
                setEnableRepairCost(false);
                s.sendMessage(textWithPrefixSuccess("Repair cost disabled"));
                break;
            }
            case "reset": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                if (args[2].equalsIgnoreCase("enable")) {
                    setEnableRepairReset(true);
                    s.sendMessage(textWithPrefixSuccess("Repair cost reset enabled"));
                } else if (args[2].equalsIgnoreCase("disable")) {
                    setEnableRepairReset(false);
                    s.sendMessage(textWithPrefixSuccess("Repair cost reset disabled"));
                } else {
                    s.sendMessage(textWithPrefixError("You must specify 'enable' or 'disable'"));
                }

                break;
            }
            case "base": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    setBaseRepairCost(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Base value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }
            }
            case "multiply": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    setRepairCostMultiplier(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Multiply value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }
            }
            default: {
                s.sendMessage(textWithPrefixError("Unknown argument"));
            }
        }
    }
}
