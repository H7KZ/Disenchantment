package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.enabled;

public class Status {
    public static final CommandBuilder command = new CommandBuilder(
            "status",
            PermissionGroupType.COMMAND_STATUS,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            Status::execute,
            Status::complete
    );

    public static void execute(CommandSender s, String[] ignoredArgs) {
        String builder = "";

        builder += ChatColor.GRAY + "Plugin is globally ";
        builder += enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

        s.sendMessage(builder);
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
