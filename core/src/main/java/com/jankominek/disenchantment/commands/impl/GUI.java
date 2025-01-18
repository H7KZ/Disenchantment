package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.guis.impl.NavigationGUI;
import com.jankominek.disenchantment.permissions.PermissionGroups;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GUI {
    public static final CommandBuilder command = new CommandBuilder(
            "gui",
            PermissionGroups.COMMAND_GUI,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            GUI::execute,
            GUI::complete
    );

    public static void execute(CommandSender s, String[] args) {
        NavigationGUI navigationGUI = new NavigationGUI();

        if (!(s instanceof Player player)) return;

        player.openInventory(navigationGUI.getInventory());
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
