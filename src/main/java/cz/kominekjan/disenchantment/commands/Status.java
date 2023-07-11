package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Status {
    public static final CommandUnit unit = new CommandUnit("status", "disenchantment.status", "You don't have permission to use this command.", new String[]{}, false, Status::command);

    public static void command(CommandSender s, String[] ignoredArgs) {
        sendMessage(s, enabled ? "enabled" : "disabled", enabled ? ChatColor.GREEN : ChatColor.RED);
        config.getStringList("disabled-worlds").forEach(world -> sendMessage(s, "disabled in world " + world, ChatColor.RED));
    }
}
