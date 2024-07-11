package cz.kominekjan.disenchantment.commands;

import cz.kominekjan.disenchantment.Disenchantment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;
import static cz.kominekjan.disenchantment.utils.TextUtil.TextWithPrefixError;
import static cz.kominekjan.disenchantment.utils.TextUtil.TextWithPrefixSuccess;

public class Toggle {
    public static final CommandUnit unit = new CommandUnit("toggle", "disenchantment.toggle", "You don't have permission to use this command.", new String[]{}, false, Toggle::command);

    public static void command(CommandSender s, String[] args) {
        if (args.length == 1) {
            Disenchantment.toggle();
            String builder = "";
            builder += ChatColor.GRAY + "Plugin is ";
            builder += Disenchantment.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            s.sendMessage(builder);
            return;
        }

        String world = args[1];

        if (config.getStringList("disabled-worlds").contains(world)) {
            List<String> disabledWorlds = config.getStringList("disabled-worlds");

            disabledWorlds.remove(world);

            config.set("disabled-worlds", disabledWorlds);
            plugin.saveConfig();

            s.sendMessage(TextWithPrefixSuccess("Enabled in world \"" + world + "\""));
        } else {
            if (!plugin.getServer().getWorlds().contains(plugin.getServer().getWorld(world))) {
                s.sendMessage(TextWithPrefixError("World \"" + world + "\" does not exist!"));
                return;
            }

            List<String> disabledWorlds = config.getStringList("disabled-worlds");

            disabledWorlds.add(world);

            config.set("disabled-worlds", disabledWorlds);
            plugin.saveConfig();

            s.sendMessage(TextWithPrefixSuccess("Disabled in world \"" + world + "\""));
        }
    }
}
