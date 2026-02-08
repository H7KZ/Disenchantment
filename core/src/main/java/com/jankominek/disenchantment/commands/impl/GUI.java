package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.guis.impl.NavigationGUI;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "gui" subcommand which opens the plugin's navigation GUI
 * for players. Console senders are silently ignored since GUIs require
 * a player context.
 */
public class GUI {
    /**
     * The command definition for the gui subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "gui",
            PermissionGroupType.COMMAND_GUI,
            new String[]{},
            false,
            GUI::execute,
            GUI::complete
    );

    /**
     * Opens the navigation GUI for the sender if they are a player.
     *
     * @param s    the command sender (must be a Player to open the GUI)
     * @param args the command arguments (unused)
     */
    public static void execute(CommandSender s, String[] args) {
        NavigationGUI navigationGUI = new NavigationGUI();

        if (!(s instanceof Player player)) return;

        player.openInventory(navigationGUI.getInventory());
    }

    /**
     * Returns an empty list since the gui command has no arguments to complete.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return an empty list
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
