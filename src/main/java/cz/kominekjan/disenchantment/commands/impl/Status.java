package cz.kominekjan.disenchantment.commands.impl;

import cz.kominekjan.disenchantment.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;

public class Status {
    public static final Command command = new Command("status", "disenchantment.status", "You don't have permission to use this command.", new String[]{}, false, Status::execute);

    public static void execute(CommandSender s, String[] ignoredArgs) {
        String builder = "";
        builder += ChatColor.GRAY + "Plugin is globally ";
        builder += enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
        s.sendMessage(builder);

        List<String> disabledWorlds = getDisabledWorlds();

        if (disabledWorlds.isEmpty()) {
            s.sendMessage(ChatColor.GRAY + "No worlds are disabled");
            return;
        }

        s.sendMessage(ChatColor.GRAY + "Disabled worlds:");
        s.sendMessage("");

        for (String world : disabledWorlds) {
            builder = "";
            builder += ChatColor.RED + "[X] ";
            builder += ChatColor.GRAY + "\"" + world + "\"";
            s.sendMessage(builder);
        }
    }
}
