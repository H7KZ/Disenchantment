package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Help {
    public static final CommandBuilder command = new CommandBuilder(
            "help",
            PermissionGroupType.COMMAND_HELP,
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"},
            false,
            Help::execute,
            Help::complete
    );

    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Help.title() + "1/" + Help.command.args.length);
            for (String help : I18n.Commands.Help.pages().get(1)) {
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

        page = Math.min(page, Help.command.args.length);
        page = Math.max(page, 1);

        List<String> pageItems = I18n.Commands.Help.pages().get(page);

        s.sendMessage(I18n.Commands.Help.title() + page + "/" + Help.command.args.length);
        for (String help : pageItems) {
            s.sendMessage(help);
        }
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : Help.command.args) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) result.add(arg);
        }

        return result;
    }
}
