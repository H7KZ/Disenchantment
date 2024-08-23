package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;

public class Status {
    public static final Command command = new Command(
            "status",
            new String[]{"disenchantment.all", "disenchantment.command.status"},
            "You don't have permission to use this command.",
            new String[]{},
            false,
            Status::execute
    );

    public static void execute(CommandSender s, String[] ignoredArgs) {
        String builder = "";

        builder += ChatColor.GRAY + "Plugin is globally ";
        builder += enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";

        s.sendMessage(builder);
    }
}
