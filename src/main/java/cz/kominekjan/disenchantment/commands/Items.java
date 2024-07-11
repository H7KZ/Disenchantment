package cz.kominekjan.disenchantment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.utils.TextUtil.TextWithPrefix;
import static cz.kominekjan.disenchantment.utils.TextUtil.TextWithPrefixSuccess;

public class Items {
    public static final CommandUnit unit = new CommandUnit("items", "disenchantment.items", "You don't have permission to use this command.", new String[]{}, false, Items::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(TextWithPrefix("Disabled items"));
            s.sendMessage("");

            List<String> list = config.getStringList("disabled-items");

            if (list.isEmpty()) {
                s.sendMessage(ChatColor.GRAY + "No items are disabled");
                return;
            }

            for (String item : list) {
                String builder = "";
                builder += ChatColor.RED + "[X] ";
                builder += ChatColor.GRAY + item;
                s.sendMessage(builder);
            }

            return;
        }

        String item = args[1];

        List<String> list = config.getStringList("disabled-items");

        if (list.contains(item)) {
            list.remove(item);

            config.set("disabled-items", list);
            plugin.saveConfig();

            s.sendMessage(TextWithPrefixSuccess("Item enabled"));
            return;
        }

        list.add(item);

        config.set("disabled-items", list);
        plugin.saveConfig();

        s.sendMessage(TextWithPrefixSuccess("Item disabled"));
    }
}
