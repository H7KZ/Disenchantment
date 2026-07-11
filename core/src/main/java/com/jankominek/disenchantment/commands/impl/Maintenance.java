package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "maintenance" subcommand which toggles the session-only maintenance
 * flag on or off. Maintenance state is never persisted and always resets to
 * disabled on server restart.
 */
public class Maintenance {
    public static final CommandBuilder command = new CommandBuilder(
            "maintenance",
            PermissionGroupType.COMMAND_MAINTENANCE,
            new String[]{"on", "off"},
            true,
            Maintenance::execute,
            Maintenance::complete
    );

    public static void execute(CommandSender s, String[] args) {
        boolean enable = args[1].equalsIgnoreCase("on");

        Disenchantment.maintenanceEnabled = enable;

        s.sendMessage(I18n.getPrefix() + " " + (enable ? I18n.Messages.maintenanceIsEnabled() : I18n.Messages.maintenanceIsDisabled()));
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        for (String arg : Maintenance.command.args()) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
        }

        return result;
    }
}
