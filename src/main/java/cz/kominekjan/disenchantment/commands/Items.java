package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.*;

public class Items {
    public static final CommandUnit unit = new CommandUnit("items", "disenchantment.items", "You don't have permission to use this command.", new String[]{}, false, Items::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            sendMessage(s, "Disabled items", ChatColor.GRAY);
            s.sendMessage(ChatColor.GRAY + "|");

            List<String> list = config.getStringList("disabled-items");

            if (list.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "| No items are disabled");
                return;
            }

            config.getStringList("disabled-items").forEach(item -> s.sendMessage(ChatColor.RED + "[X]" + ChatColor.GRAY + "\"" + item + "\""));
            return;
        }

        String item = args[1];

        List<String> list = config.getStringList("disabled-items");

        if (list.contains(item)) {
            list.remove(item);

            config.set("disabled-items", list);
            plugin.saveConfig();

            sendMessage(s, "Item enabled", ChatColor.GREEN);
            return;
        }

        list.add(item);

        config.set("disabled-items", list);
        plugin.saveConfig();

        sendMessage(s, "Item disabled", ChatColor.GREEN);
    }
}
