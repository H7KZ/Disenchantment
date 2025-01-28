package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Toggle {
    public static final CommandBuilder command = new CommandBuilder(
            "toggle",
            PermissionGroupType.COMMAND_TOGGLE,
            new String[]{},
            false,
            Toggle::execute,
            Toggle::complete
    );

    public static void execute(CommandSender s, String[] args) {
        boolean pluginEnabled = !Config.isPluginEnabled();

        Disenchantment.onToggle(pluginEnabled);
        Config.setPluginEnabled(pluginEnabled);

        String builder = "";

        builder += ChatColor.GRAY + "Plugin is ";
        builder += Disenchantment.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

        s.sendMessage(builder);
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
