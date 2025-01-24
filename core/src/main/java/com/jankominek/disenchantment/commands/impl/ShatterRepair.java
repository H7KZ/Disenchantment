package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.utils.TextUtils.*;

public class ShatterRepair {
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(textWithPrefix("Repair cost configuration"));
            s.sendMessage("");

            String builder = "";

            builder += ChatColor.GRAY + "Repair reset is ";
            builder += Config.Shatterment.Anvil.Repair.isResetEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

            builder += ChatColor.GRAY + "Repair cost is ";
            builder += Config.Shatterment.Anvil.Repair.isCostEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

            s.sendMessage(builder);
            s.sendMessage(ChatColor.GRAY + "Base value: " + Config.Shatterment.Anvil.Repair.getBaseCost());
            s.sendMessage(ChatColor.GRAY + "Multiply value: " + Config.Shatterment.Anvil.Repair.getCostMultiplier());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "reset": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                if (args[2].equalsIgnoreCase("enable")) {
                    Config.Shatterment.Anvil.Repair.setResetEnabled(true);
                    s.sendMessage(textWithPrefixSuccess("Repair cost reset enabled"));
                } else if (args[2].equalsIgnoreCase("disable")) {
                    Config.Shatterment.Anvil.Repair.setResetEnabled(false);
                    s.sendMessage(textWithPrefixSuccess("Repair cost reset disabled"));
                } else {
                    s.sendMessage(textWithPrefixError("You must specify 'enable' or 'disable'"));
                }

                break;
            }
            case "cost": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                if (args[2].equalsIgnoreCase("enable")) {
                    Config.Shatterment.Anvil.Repair.setCostEnabled(true);
                    s.sendMessage(textWithPrefixSuccess("Repair cost reset enabled"));
                } else if (args[2].equalsIgnoreCase("disable")) {
                    Config.Shatterment.Anvil.Repair.setCostEnabled(false);
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
                    Config.Shatterment.Anvil.Repair.setBaseCost(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Base value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }

                break;
            }
            case "multiply": {
                if (args.length == 2) {
                    s.sendMessage(textWithPrefixError("You must specify a value"));
                    break;
                }

                try {
                    Config.Shatterment.Anvil.Repair.setCostMultiplier(Double.parseDouble(args[2]));
                    s.sendMessage(textWithPrefixSuccess("Multiply value set to " + args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(textWithPrefixError("You must specify a valid number"));
                }

                break;
            }
            default: {
                s.sendMessage(textWithPrefixError("Unknown argument"));
            }
        }
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (String arg : ShatterRepair.command.args) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(arg);
                }
            }
        } else if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "reset":
                case "cost":
                    if ("enable".startsWith(args[2].toLowerCase())) {
                        result.add("enable");
                    }

                    if ("disable".startsWith(args[2].toLowerCase())) {
                        result.add("disable");
                    }

                    break;
            }
        }

        return result;
    }

    public static final CommandBuilder command = new CommandBuilder(
            "shatter:repair",
            PermissionGroupType.COMMAND_SHATTER_REPAIR,
            "You don't have permission to use this command.",
            new String[]{"reset", "cost", "base", "multiply"},
            false,
            ShatterRepair::execute,
            ShatterRepair::complete
    );


}
