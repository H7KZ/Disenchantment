package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "toggle" subcommand which toggles the enabled/disabled state of
 * the plugin globally, or for the disenchantment or shatterment features
 * individually. Displays the status after toggling.
 */
public class Toggle {
    /**
     * The command definition for the toggle subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "toggle",
            PermissionGroupType.COMMAND_TOGGLE,
            new String[]{"global", "disenchantment", "shatterment"},
            false,
            Toggle::execute,
            Toggle::complete
    );

    /**
     * Toggles the specified feature. With no extra arguments, displays the current status.
     * Supports "global", "disenchantment", and "shatterment" as toggle targets.
     *
     * @param s    the command sender
     * @param args the command arguments; args[1] is the optional toggle target
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            Status.execute(s, args);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "global": {
                boolean pluginEnabled = !Config.isPluginEnabled();

                Disenchantment.onToggle(pluginEnabled);
                Config.setPluginEnabled(pluginEnabled);

                Status.execute(s, args);

                break;
            }
            case "disenchantment": {
                boolean disenchantmentEnabled = !Config.Disenchantment.isEnabled();

                Config.Disenchantment.setEnabled(disenchantmentEnabled);

                Status.execute(s, args);

                break;
            }
            case "shatterment": {
                boolean shattermentEnabled = !Config.Shatterment.isEnabled();

                Config.Shatterment.setEnabled(shattermentEnabled);

                Status.execute(s, args);

                break;
            }
        }
    }

    /**
     * Provides tab completion suggestions for toggle target names.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching toggle target suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : Toggle.command.args()) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
        }

        return result;
    }
}
