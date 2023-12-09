package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Status {
    public static final CommandUnit unit = new CommandUnit("status", "disenchantment.status", "You don't have permission to use this command.", new String[]{}, false, Status::command);

    public static void command(CommandSender s, String[] ignoredArgs) {
        sendMessage(s, "Globally " + (enabled ? "Enabled" : "Disabled"), enabled ? ChatColor.GREEN : ChatColor.RED);

        List<String> disabledWorlds = config.getStringList("disabled-worlds");

        if (disabledWorlds.isEmpty()) {
            sendMessage(s, "No worlds are disabled", ChatColor.GRAY);
            return;
        }

        sendMessage(s, "Disabled worlds", ChatColor.GRAY);
        s.sendMessage(ChatColor.GRAY + "|");
        disabledWorlds.forEach(world -> s.sendMessage(ChatColor.RED + "[X] \"" + world + "\""));
    }
}
