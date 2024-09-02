package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.guis.impl.NavigationGUI;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GUI {
    public static final Command command = new Command(
            "gui",
            PermissionGoal.GUI_CMD,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            GUI::execute
    );

    public static void execute(CommandSender s, String[] args) {
        NavigationGUI navigationGUI = new NavigationGUI();

        if (!(s instanceof Player player)) return;

        player.openInventory(navigationGUI.getInventory());
    }
}
