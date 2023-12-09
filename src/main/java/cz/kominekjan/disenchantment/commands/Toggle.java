package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Toggle {
    public static final CommandUnit unit = new CommandUnit("toggle", "disenchantment.toggle", "You don't have permission to use this command.", new String[]{}, false, Toggle::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            Disenchantment.toggle();
            sendMessage(s, "Switched to " + (Disenchantment.enabled ? "enabled" : "disabled"), ChatColor.GREEN);
            return;
        }

        String world = args[1];

        if (config.getStringList("disabled-worlds").contains(world)) {
            List<String> disabledWorlds = config.getStringList("disabled-worlds");

            disabledWorlds.remove(world);

            config.set("disabled-worlds", disabledWorlds);
            plugin.saveConfig();

            sendMessage(s, "Enabled in world \"" + world + "\"", ChatColor.GREEN);
        } else {
            if (!plugin.getServer().getWorlds().contains(plugin.getServer().getWorld(world))) {
                sendMessage(s, "world " + world + " does not exist!", ChatColor.RED);
                return;
            }

            List<String> disabledWorlds = config.getStringList("disabled-worlds");

            disabledWorlds.add(world);

            config.set("disabled-worlds", disabledWorlds);
            plugin.saveConfig();

            sendMessage(s, "Disabled in world \"" + world + "\"", ChatColor.GREEN);
        }
    }
}
