package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "debug" subcommand which toggles debug logging at runtime.
 * The toggle is session-only and does not write to config.yml.
 * Use {@code logging.level: DEBUG} in config.yml for a persistent setting.
 */
public class Debug {
    public static final CommandBuilder command = new CommandBuilder(
            "debug",
            PermissionGroupType.COMMAND_DEBUG,
            new String[]{"on", "off"},
            false,
            Debug::execute,
            Debug::complete
    );

    /**
     * Toggles debug mode on or off. With no extra arguments, displays the current status.
     * Supports "on" and "off" as toggle targets.
     *
     * @param sender the command sender
     * @param args   the command arguments; args[1] is the optional toggle target
     */
    public static void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(I18n.getPrefix() + " Debug mode: " + (DiagnosticUtils.isDebugEnabled() ? "ON" : "OFF"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "on" -> {
                DiagnosticUtils.setDebugEnabled(true);
                sender.sendMessage(I18n.getPrefix() + " Debug mode: ON");
            }
            case "off" -> {
                DiagnosticUtils.setDebugEnabled(false);
                sender.sendMessage(I18n.getPrefix() + " Debug mode: OFF");
            }
            default -> sender.sendMessage(I18n.getPrefix() + " Usage: /disenchantment debug [on|off]");
        }
    }

    /**
     * Provides tab completion suggestions for debug mode toggle options.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching toggle option suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            for (String arg : Debug.command.args()) {
                if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
            }
        }
        return result;
    }
}
