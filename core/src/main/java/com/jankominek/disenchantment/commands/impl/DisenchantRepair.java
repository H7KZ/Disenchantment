package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "disenchant:repair" subcommand for managing anvil repair cost settings
 * in the disenchantment feature. Supports viewing and modifying repair cost, cost reset,
 * base cost, and cost multiplier settings.
 */
public class DisenchantRepair {
    /**
     * The command definition for the disenchant:repair subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:repair",
            PermissionGroupType.COMMAND_DISENCHANT_REPAIR,
            new String[]{"reset", "cost", "base", "multiply"},
            false,
            DisenchantRepair::execute,
            DisenchantRepair::complete
    );

    /**
     * Executes the disenchant:repair command. With no extra arguments, displays current
     * repair settings. With a setting name and value, updates the specified repair
     * configuration (reset, cost, base, or multiply).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, setting, value]
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Repair.Disenchantment.title());
            s.sendMessage(
                    I18n.Commands.Repair.Disenchantment.cost(
                            Config.Disenchantment.Anvil.Repair.isCostEnabled() ?
                                    I18n.Commands.Repair.Disenchantment.States.enabled() :
                                    I18n.Commands.Repair.Disenchantment.States.disabled()
                    )
            );
            s.sendMessage(
                    I18n.Commands.Repair.Disenchantment.reset(
                            Config.Disenchantment.Anvil.Repair.isResetEnabled() ?
                                    I18n.Commands.Repair.Disenchantment.States.enabled() :
                                    I18n.Commands.Repair.Disenchantment.States.disabled()
                    )
            );
            s.sendMessage(I18n.Commands.Repair.Disenchantment.base(String.valueOf(Config.Disenchantment.Anvil.Repair.getBaseCost())));
            s.sendMessage(I18n.Commands.Repair.Disenchantment.multiply(String.valueOf(Config.Disenchantment.Anvil.Repair.getCostMultiplier())));

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
                        Config.Disenchantment.Anvil.Repair.setResetEnabled(true);

                        s.sendMessage(I18n.Messages.repairCostResetIsEnabled());

                        break;
                    }
                    case "disable": {
                        Config.Disenchantment.Anvil.Repair.setResetEnabled(false);

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
                        Config.Disenchantment.Anvil.Repair.setCostEnabled(true);

                        s.sendMessage(I18n.Messages.repairCostResetIsEnabled());

                        break;
                    }
                    case "disable": {
                        Config.Disenchantment.Anvil.Repair.setCostEnabled(false);

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
                    Config.Disenchantment.Anvil.Repair.setBaseCost(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.repairBaseCostIsSet(Config.Disenchantment.Anvil.Repair.getBaseCost().toString()));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            case "multiply": {
                try {
                    Config.Disenchantment.Anvil.Repair.setCostMultiplier(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.repairMultiplierIsSet(Config.Disenchantment.Anvil.Repair.getCostMultiplier().toString()));
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

    /**
     * Provides tab completion suggestions. At position 2, suggests repair setting names;
     * at position 3, suggests enable/disable for applicable settings.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (String arg : DisenchantRepair.command.args()) {
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
