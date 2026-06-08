package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "shatter:economy" subcommand for managing economy settings
 * in the shatterment feature. Supports viewing and modifying enabled state,
 * cost, show-cost, and charge-message settings.
 */
public class ShatterEconomy {
    /**
     * The command definition for the shatter:economy subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "shatter:economy",
            PermissionGroupType.COMMAND_SHATTER_ECONOMY,
            new String[]{"enabled", "cost", "show-cost", "charge-message"},
            false,
            ShatterEconomy::execute,
            ShatterEconomy::complete
    );

    /**
     * Executes the shatter:economy command. With no extra arguments, displays current
     * economy settings for the shatterment feature. With a setting name and value,
     * updates the specified economy configuration (enabled, cost, show-cost, or charge-message).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, setting, value]
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Economy.Shatterment.title());
            s.sendMessage(
                    I18n.Commands.Economy.Shatterment.enabled(
                            Config.Shatterment.Economy.isEnabled() ?
                                    I18n.Commands.Economy.Shatterment.States.enabled() :
                                    I18n.Commands.Economy.Shatterment.States.disabled()
                    )
            );
            s.sendMessage(I18n.Commands.Economy.Shatterment.cost(String.valueOf(Config.Shatterment.Economy.getCost())));
            s.sendMessage(
                    I18n.Commands.Economy.Shatterment.showCost(
                            Config.Shatterment.Economy.isShowCostEnabled() ?
                                    I18n.Commands.Economy.Shatterment.States.enabled() :
                                    I18n.Commands.Economy.Shatterment.States.disabled()
                    )
            );
            s.sendMessage(
                    I18n.Commands.Economy.Shatterment.chargeMessage(
                            Config.Shatterment.Economy.isChargeMessageEnabled() ?
                                    I18n.Commands.Economy.Shatterment.States.enabled() :
                                    I18n.Commands.Economy.Shatterment.States.disabled()
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
                        Config.Shatterment.Economy.setEnabled(true);

                        s.sendMessage(I18n.Messages.economyEnabledIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Shatterment.Economy.setEnabled(false);

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
                    Config.Shatterment.Economy.setCost(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.economyCostIsSet(String.valueOf(Config.Shatterment.Economy.getCost())));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            case "show-cost": {
                switch (args[2].toLowerCase()) {
                    case "true": {
                        Config.Shatterment.Economy.setShowCostEnabled(true);

                        s.sendMessage(I18n.Messages.economyShowCostIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Shatterment.Economy.setShowCostEnabled(false);

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
                        Config.Shatterment.Economy.setChargeMessageEnabled(true);

                        s.sendMessage(I18n.Messages.economyChargeMessageIsEnabled());

                        break;
                    }
                    case "false": {
                        Config.Shatterment.Economy.setChargeMessageEnabled(false);

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
            for (String arg : ShatterEconomy.command.args()) {
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
