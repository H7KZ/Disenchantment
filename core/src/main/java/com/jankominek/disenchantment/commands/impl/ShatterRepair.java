package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ShatterRepair {
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:repair",
            PermissionGroupType.COMMAND_SHATTER_REPAIR,
            new String[]{"reset", "cost", "base", "multiply"},
            false,
            ShatterRepair::execute,
            ShatterRepair::complete
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Repair.Shatterment.title());
            s.sendMessage(
                    I18n.Commands.Repair.Shatterment.cost(
                            Config.Shatterment.Anvil.Repair.isCostEnabled() ?
                                    I18n.Commands.Repair.Shatterment.States.enabled() :
                                    I18n.Commands.Repair.Shatterment.States.disabled()
                    )
            );
            s.sendMessage(
                    I18n.Commands.Repair.Shatterment.reset(
                            Config.Shatterment.Anvil.Repair.isResetEnabled() ?
                                    I18n.Commands.Repair.Shatterment.States.enabled() :
                                    I18n.Commands.Repair.Shatterment.States.disabled()
                    )
            );
            s.sendMessage(I18n.Commands.Repair.Shatterment.base(String.valueOf(Config.Shatterment.Anvil.Repair.getBaseCost())));
            s.sendMessage(I18n.Commands.Repair.Shatterment.multiply(String.valueOf(Config.Shatterment.Anvil.Repair.getCostMultiplier())));

            return;
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyRepairValue());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "reset": {
                switch (args[2].toLowerCase()) {
                    case "enable": {
                        Config.Shatterment.Anvil.Repair.setResetEnabled(true);

                        s.sendMessage(I18n.Messages.repairCostResetIsEnabled());

                        break;
                    }
                    case "disable": {
                        Config.Shatterment.Anvil.Repair.setResetEnabled(false);

                        s.sendMessage(I18n.Messages.repairCostIsDisabled());

                        break;
                    }
                    default: {
                        s.sendMessage(I18n.Messages.specifyRepairValue());
                    }
                }

                break;
            }
            case "cost": {
                switch (args[2].toLowerCase()) {
                    case "enable": {
                        Config.Shatterment.Anvil.Repair.setCostEnabled(true);

                        s.sendMessage(I18n.Messages.repairCostResetIsEnabled());

                        break;
                    }
                    case "disable": {
                        Config.Shatterment.Anvil.Repair.setCostEnabled(false);

                        s.sendMessage(I18n.Messages.repairCostIsDisabled());

                        break;
                    }
                    default: {
                        s.sendMessage(I18n.Messages.specifyRepairValue());
                    }
                }

                break;
            }
            case "base": {
                try {
                    Config.Shatterment.Anvil.Repair.setBaseCost(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.repairBaseCostIsSet(Config.Shatterment.Anvil.Repair.getBaseCost().toString()));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            case "multiply": {
                try {
                    Config.Shatterment.Anvil.Repair.setCostMultiplier(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.repairMultiplierIsSet(Config.Shatterment.Anvil.Repair.getCostMultiplier().toString()));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            default: {
                s.sendMessage(I18n.Messages.invalidArgument());
            }
        }
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (String arg : ShatterRepair.command.args) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
            }
        }

        if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "reset":
                case "cost":
                    if ("enable".startsWith(args[2].toLowerCase())) result.add("enable");
                    if ("disable".startsWith(args[2].toLowerCase())) result.add("disable");
                    break;
            }
        }

        return result;
    }
}
