package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.commands.Command;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Toggle {
    public static final Command command = new Command(
            "toggle",
            PermissionGoal.TOGGLE_CMD,
            "You don't have permission to use this command.",
            new String[]{},
            false,
            Toggle::execute
    );

    public static void execute(CommandSender s, String[] args) {
        boolean pluginEnabled = !Config.isPluginEnabled();

        Disenchantment.toggle(pluginEnabled);
        Config.setPluginEnabled(pluginEnabled);

        String builder = "";

        builder += ChatColor.GRAY + "Plugin is ";
        builder += Disenchantment.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

        s.sendMessage(builder);
    }
}
