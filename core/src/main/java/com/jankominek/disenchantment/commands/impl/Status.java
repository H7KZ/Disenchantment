package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.enabled;

public class Status {
    public static final CommandBuilder command = new CommandBuilder(
            "status",
            PermissionGroupType.COMMAND_STATUS,
            new String[]{},
            false,
            Status::execute,
            Status::complete
    );

    public static void execute(CommandSender s, String[] ignoredArgs) {
        s.sendMessage(I18n.Commands.Status.title());
        s.sendMessage(I18n.Commands.Status.global(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.disenchantment(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
        s.sendMessage(I18n.Commands.Status.shatterment(enabled ? I18n.Commands.Status.States.enabled() : I18n.Commands.Status.States.disabled()));
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
