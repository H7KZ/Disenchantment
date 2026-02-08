package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "help" subcommand which displays paginated help information
 * about the plugin's available commands. Defaults to page 1 when no page
 * number is specified.
 */
public class Help {
    /**
     * The command definition for the help subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "help",
            PermissionGroupType.COMMAND_HELP,
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"},
            false,
            Help::execute,
            Help::complete
    );

    /**
     * Displays the specified help page to the sender. Defaults to page 1 if no
     * page number is provided, and clamps invalid page numbers to the valid range.
     *
     * @param s    the command sender
     * @param args the command arguments; args[1] is the optional page number
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Help.title("1/" + Help.command.args().length));
            for (String help : I18n.Commands.Help.pages().get(0)) {
                s.sendMessage(help);
            }

            return;
        }

        int page;

        try {
            page = Integer.parseInt(args[1]);
        } catch (Exception e) {
            s.sendMessage(I18n.Messages.invalidArgument());
            return;
        }

        page = Math.min(page, Help.command.args().length);
        page = Math.max(page, 1);

        List<String> pageItems = I18n.Commands.Help.pages().get(page - 1);

        s.sendMessage(I18n.Commands.Help.title(page + "/" + Help.command.args().length));
        for (String help : pageItems) {
            s.sendMessage(help);
        }
    }

    /**
     * Provides tab completion suggestions for help page numbers.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching page number suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : Help.command.args()) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
        }

        return result;
    }
}
