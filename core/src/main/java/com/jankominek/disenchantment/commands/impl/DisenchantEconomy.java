package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "disenchant:economy" subcommand for managing economy settings
 * in the disenchantment feature. Supports viewing and modifying enabled state,
 * cost, show-cost, and charge-message settings.
 */
public class DisenchantEconomy {
    /**
     * The command definition for the disenchant:economy subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:economy",
            PermissionGroupType.COMMAND_DISENCHANT_ECONOMY,
            new String[]{"enabled", "cost", "show-cost", "charge-message"},
            false,
            DisenchantEconomy::execute,
            DisenchantEconomy::complete
    );

    /**
     * Executes the disenchant:economy command. With no extra arguments, displays current
     * economy settings. With a setting name and value, updates the specified economy
     * configuration (enabled, cost, show-cost, or charge-message).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, setting, value]
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Economy.Disenchantment.title());
            s.sendMessage(
                    I18n.Commands.Economy.Disenchantment.enabled(
                            Config.Disenchantment.Economy.isEnabled() ?
                                    I18n.Commands.Economy.Disenchantment.States.enabled() :
                                    I18n.Commands.Economy.Disenchantment.States.disabled()
                    )
            );
            s.sendMessage(I18n.Commands.Economy.Disenchantment.cost(String.valueOf(Config.Disenchantment.Economy.getCost())));
            s.sendMessage(
                    I18n.Commands.Economy.Disenchantment.showCost(
                            Config.Disenchantment.Economy.isShowCostEnabled() ?
                                    I18n.Commands.Economy.Disenchantment.States.enabled() :
                                    I18n.Commands.Economy.Disenchantment.States.disabled()
                    )
            );
            s.sendMessage(
                    I18n.Commands.Economy.Disenchantment.chargeMessage(
                            Config.Disenchantment.Economy.isChargeMessageEnabled() ?
                                    I18n.Commands.Economy.Disenchantment.States.enabled() :
                                    I18n.Commands.Economy.Disenchantment.States.disabled()
                    )
            );

            return;
        }

        if (args.length == 2) {
            s.sendMessage(I18n.Messages.specifyRepairValue());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enabled": {
                switch (args[2].toLowerCase()) {
                    case "true": {
                        Config.Disenchantment.Economy.setEnabled(true);

                        s.sendMessage(I18n.Messages.economyEnabledIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Disenchantment.Economy.setEnabled(false);

                        s.sendMessage(I18n.Messages.economyEnabledIsDisabled());

                        break;
                    }
                    default: {
                        s.sendMessage(I18n.Messages.specifyRepairValue());
                    }
                }

                break;
            }
            case "cost": {
                try {
                    Config.Disenchantment.Economy.setCost(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.economyCostIsSet(String.valueOf(Config.Disenchantment.Economy.getCost())));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            case "show-cost": {
                switch (args[2].toLowerCase()) {
                    case "true": {
                        Config.Disenchantment.Economy.setShowCostEnabled(true);

                        s.sendMessage(I18n.Messages.economyShowCostIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Disenchantment.Economy.setShowCostEnabled(false);

                        s.sendMessage(I18n.Messages.economyShowCostIsDisabled());

                        break;
                    }
                    default: {
                        s.sendMessage(I18n.Messages.specifyRepairValue());
                    }
                }

                break;
            }
            case "charge-message": {
                switch (args[2].toLowerCase()) {
                    case "true": {
                        Config.Disenchantment.Economy.setChargeMessageEnabled(true);

                        s.sendMessage(I18n.Messages.economyChargeMessageIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Disenchantment.Economy.setChargeMessageEnabled(false);

                        s.sendMessage(I18n.Messages.economyChargeMessageIsDisabled());

                        break;
                    }
                    default: {
                        s.sendMessage(I18n.Messages.specifyRepairValue());
                    }
                }

                break;
            }
            default: {
                s.sendMessage(I18n.Messages.invalidArgument());
            }
        }
    }

    /**
     * Provides tab completion suggestions. At position 2, suggests economy setting names;
     * at position 3, suggests true/false for applicable settings.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        if (args.length == 2) {
            for (String arg : DisenchantEconomy.command.args()) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
            }
        }

        if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "enabled":
                case "show-cost":
                case "charge-message":
                    if ("true".startsWith(args[2].toLowerCase())) result.add("true");
                    if ("false".startsWith(args[2].toLowerCase())) result.add("false");
                    break;
            }
        }

        return result;
    }
}
