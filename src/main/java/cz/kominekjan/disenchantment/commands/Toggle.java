package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static cz.kominekjan.disenchantment.Disenchantment.sendMessage;

public class Toggle {
    public static final CommandUnit unit = new CommandUnit("toggle", "disenchantment.toggle", "You don't have permission to use this command.", new String[]{}, false, Toggle::command);

    public static boolean command(CommandSender s, String[] ignoredArgs) {
        Disenchantment.toggle();

        sendMessage(s, "now " + (Disenchantment.enabled ? "enabled" : "disabled"), ChatColor.DARK_GREEN);

        return true;
    }
}
